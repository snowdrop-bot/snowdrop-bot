package io.snowdrop.github.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.StatusLogger;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.Parent;
import io.snowdrop.github.reporting.model.Repository;
import io.snowdrop.reporting.model.Issue;

public class IssueCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(IssueCollector.class);
  private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  private final StatusLogger status;

  private final GitHubClient client;
  private final IssueService issueService;
  private final RepositoryService repositoryService;


  private final Set<String> users;
  private final Set<String> organizations;
//  private final Set<String> snowdropZenRepositories;

  private final String targetOrganization;

  // These dates represent current reporting period
  private ZonedDateTime startTime;
  private ZonedDateTime endTime;

  // These represent the oldest reporting period possible
  private Date minStartTime;
  private Date minEndTime;

  private int reportingDay;
  private int reportingHour;

  public IssueCollector(
      GitHubClient client, StatusLogger status, int reportingDay, int reportingHour, Set<String> users,
      Set<String> organizations, String targetOrganization) {
    this.client = client;
    this.status = status;
    this.issueService = new IssueService(client);
    this.repositoryService = new RepositoryService(client);
    this.users = users;
    this.organizations = organizations;
//    this.snowdropZenRepositories = snowdropZenRepositories;
    this.reportingDay = reportingDay;
    this.reportingHour = reportingHour;
    this.targetOrganization = targetOrganization;
    setDates();
  }

  public void setDates() {
    this.endTime = ZonedDateTime.now().with(DayOfWeek.of(reportingDay)).withHour(reportingHour);
    this.startTime = endTime.minusWeeks(1);
    this.minStartTime = Date.from(startTime.minusMonths(6).toInstant());
    this.minEndTime = Date.from(endTime.toInstant());
  }

  public void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectIssues();
  }

  public Map<String, Set<Issue>> collectIssues() {
    return streamIssues().collect(Collectors.groupingBy(Issue::getAssignee, Collectors.toSet()));
  }

  public Stream<Issue> streamIssues() {
    setDates();
    LOGGER.info("Streaming issues: {}-{}, {}-{}", startTime, endTime, minStartTime, minEndTime);
    Set<String> snowdropZenRepositories = userForks(targetOrganization).map(repository -> repository.getName()).collect(Collectors.toSet());
    long total = Repository.<Repository>streamAll()
        .filter(repository -> !(snowdropZenRepositories.contains(repository.getName()) && !repository.getOwner().equals(targetOrganization)))
        .map(r -> {
          if(Parent.NONE.equals(r.getParent()) || targetOrganization.equals(r.getOwner()))
            return r.getOwner() + "/" + r.getName();
          return r.getParent();
        })
        .distinct()
        .count();

    List<String> sorted = Repository.<Repository>streamAll()
    .filter(repository -> !(snowdropZenRepositories.contains(repository.getName()) && !repository.getOwner().equals(targetOrganization)))
    .map(r -> {
      if (Parent.NONE.equals(r.getParent()) || targetOrganization.equals(r.getOwner()))
        return r.getOwner() + "/" + r.getName();
      return r.getParent();
    })
    .distinct()
    .sorted().collect(Collectors.toList());
    return sorted.stream()
        .map(status.<String>log(total, "Collecting issues from repository %s."))
        .flatMap(i -> teamIssueStream(i, "all"));
  }

  /**
   * Stream all the repositories of the specified user.
   * @param user The user
   * @return A stream of {@link Repository}.
   */
  public Stream<Repository> userForks(final String user) {
    synchronized (client) {
      try {
        return repositoryService.getRepositories(user).stream().filter(r -> r.isFork())
        .map(r ->repository(user, r.getName()))
        .filter(r -> organizations.contains(r.getParent().getOwner().getLogin())).map(Repository::create);
      } catch (final IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  private org.eclipse.egit.github.core.Repository repository(String user, String name) {
    synchronized (client) {
      try {
        return repositoryService.getRepository(user, name);
      } catch (IOException e) {
        throw BotException.launderThrowable("Error reading repository:" + user + "/" + name, e);
      }
    }
  }

  public Set<Issue> userIssues(String user, Repository repository, String state) {
    return streamUserIssues(user, repository, state)
        .collect(Collectors.toSet());
  }

  public Stream<Issue> streamUserIssues(String user, Repository repository, String state) {
    synchronized (client) {

      try {
        String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
        LOGGER.info("Getting {} issues for repository: {}/{} during: {} - {}.", state, Github.user(id), Github.repo(id), DF.format(minStartTime),
            DF.format(minEndTime));
        return issueService.getIssues(Github.user(id), Github.repo(id), Github.params().state(state).build())
            .stream()
            .map(i -> Issue.create(id, i))
            .filter(i -> i.isActiveDuring(minStartTime, minEndTime))
            .map(IssueCollector::log);
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  private Stream<Issue> teamIssueStream(String repository, String state) {
    synchronized (client) {
      try {
        LOGGER.info("Getting {} team issues for repository: {}", state, repository);
        return issueService
            .getIssues(Github.user(repository), Github.repo(repository), Github.params().state(state).build()).stream()
            .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
            .map(i -> Issue.create(repository, i)).filter(i -> i.isActiveDuring(minStartTime, minEndTime))
            .map(IssueCollector::log);
      } catch (IOException e) {
        LOGGER.warn("Failed to get issues from repository: " + repository + ", due to:" + e.getMessage());
        return Stream.of();
      }
    }
  }

  /**
   * Log and return self. Lambda friendly log hack.
   */
  private static Issue log(Issue issue) {
    LOGGER.info("{}: {}. {} - {} - {}.", issue.getNumber(), issue.getTitle(), DF.format(issue.getCreatedAt()),
        issue.getUpdatedAt() != null ? DF.format(issue.getUpdatedAt()) : null,
        issue.getClosedAt() != null ? DF.format(issue.getClosedAt()) : null);
    return issue;
  }

  public Set<String> getUsers() {
    return users;
  }

  public Set<String> getOrganizations() {
    return organizations;
  }

  public Stream<Issue> getIssues() {
    return Issue.<Issue>findAll().stream();
  }

  public ZonedDateTime getStartTime() {
    return startTime;
  }

  public ZonedDateTime getEndTime() {
    return endTime;
  }

  public String getTargetOrganization() {
    return targetOrganization;
  }
}
