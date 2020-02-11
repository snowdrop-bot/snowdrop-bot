package io.snowdrop.github.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.StatusLogger;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

public class PullRequestCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(PullRequestCollector.class);
  private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  private final GitHubClient client;
  private final StatusLogger status;
  private final RepositoryService repositoryService;
  private final PullRequestService pullRequestService;
  private final int reportingDay;
  private final int reportingHour;

  private final Set<String> users;
  private final Set<String> organizations;

  // These dates represent current reporting period
  private final ZonedDateTime startTime;
  private final ZonedDateTime endTime;

  // These represent the oldest reporting period possible
  private final Date minStartTime;
  private final Date minEndTime;

  private final Map<String, Set<Repository>> repositories = new HashMap<>();

  public PullRequestCollector(GitHubClient client, StatusLogger status, int reportingDay, int reportingHour, Set<String> users,
      Set<String> organizations) {
    this.client = client;
    this.status  = status;
    this.repositoryService = new RepositoryService(client);
    this.pullRequestService = new PullRequestService(client);
    this.reportingDay = reportingDay;
    this.reportingHour = reportingHour;
    this.users = users;
    this.organizations = organizations;

    this.endTime = ZonedDateTime.now().with(DayOfWeek.of(reportingDay)).withHour(reportingHour);
    this.startTime = endTime.minusWeeks(1);

    this.minStartTime = Date.from(startTime.minusMonths(6).toInstant());
    this.minEndTime = Date.from(endTime.toInstant());
    init();
  }

  public void init() {
    users.stream().forEach(u -> {
      repositories.put(u, new HashSet<>());
    });
  }

  public void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectPullRequests();
  }

  public Map<String, Set<PullRequest>> collectPullRequests() {
    long total = Repository.<Repository>streamAll()
      .map(Repository::getParent).filter(r -> r != null)
      .distinct().count();

    return Repository.<Repository>streamAll()
      .map(Repository::getParent)
      .filter(r -> r != null).distinct()
      .sorted()
      .map(status.<String>log(total, "Collecting pull requests from repository %s."))
      .flatMap(r -> teamPullRequestStream(r, "all"))
      .collect(Collectors.groupingBy(PullRequest::getCreator, Collectors.toSet()));
  }

  /**
   * Get all the repositories of the specified user.
   *
   * @param user The user
   * @return A set of {@link Repository}.
   */
  public Set<Repository> userForks(final String user) {
    synchronized (client) {
      try {
        return repositoryService.getRepositories(user).stream().filter(r -> r.isFork())
            .map(r -> repository(user, r.getName()))
            .filter(r -> organizations.contains(r.getParent().getOwner().getLogin())).map(Repository::create)
            .collect(Collectors.toSet());
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

  private Map<String, Set<PullRequest>> teamPullRequests(final String repository, final String state) {
    return teamPullRequestStream(repository, state).collect(Collectors.groupingBy(PullRequest::getCreator, Collectors.toSet()));
  }

  private Stream<PullRequest> teamPullRequestStream(final String repository, final String state) {
    synchronized (client) {
      try {
        LOGGER.info("Getting {} pull requests for repository: {}", state, repository);
        return pullRequestService.getPullRequests(() -> repository, state).stream()
            .map(p -> PullRequest.create(repository, p))
            .map(PullRequestCollector::log)
            .filter(p -> users.contains(p.getCreator()))
            .filter(p -> p.isActiveDuring(minStartTime, minEndTime))
            .map(PullRequestCollector::log);
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  public Set<PullRequest> userPullRequests(final String user, final Repository repository, final String state) {
    synchronized (client) {
      try {
        String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
        LOGGER.info("Getting {} pull requests for repository: {}", state, id);
        return pullRequestService.getPullRequests(() -> id, state).stream()
            .filter(p -> p.getUser().getLogin().equals(user)).map(p -> PullRequest.create(id, p))
            .filter(i -> i.isActiveDuring(minStartTime, minEndTime)).map(PullRequestCollector::log)
            .collect(Collectors.toSet());
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  /**
   * Log and return self. Lambda friendly log hack.
   */
  private static PullRequest log(PullRequest pull) {
    LOGGER.info("{}: {}. {} - {} - {}.", pull.getNumber(), pull.getTitle(), DF.format(pull.getCreatedAt()),
                pull.getUpdatedAt() != null ? DF.format(pull.getUpdatedAt()) : null,
                pull.getClosedAt() != null ? DF.format(pull.getClosedAt()) : null);
    return pull;
  }

  public Set<String> getUsers() {
    return users;
  }

  public Set<String> getOrganizations() {
    return organizations;
  }

  public Map<String, Set<Repository>> getRepositories() {
    return repositories;
  }

  public Stream<PullRequest> getPullRequests() {
    return PullRequest.<PullRequest>findAll().stream();
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
}
