package io.snowdrop.jira.reporting;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.atlassian.jira.rest.client.api.JiraRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.StatusLogger;
import io.snowdrop.github.reporting.model.Repository;
import io.snowdrop.reporting.model.Issue;

public class JiraIssueCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraIssueCollector.class);

  private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy/MM/dd");

  private final StatusLogger status;

  private final JiraRestClient client;

  private final Set<String> users;

  private final Set<String> repositories;

  // These dates represent current reporting period
  private final ZonedDateTime startTime;
  private final ZonedDateTime endTime;

  // These represent the oldest reporting period possible
  private final Date minStartTime;
  private final Date minEndTime;

  public JiraIssueCollector(JiraRestClient client, StatusLogger status, int reportingDay, int reportingHour, Set<String> users, Set<String> repositories) {
    this.client = client;
    this.status = status;
    this.users = users;
    this.repositories=repositories;

    this.endTime = ZonedDateTime.now().with(DayOfWeek.of(reportingDay)).withHour(reportingHour);
    this.startTime = endTime.minusWeeks(1);

    this.minStartTime = Date.from(startTime.minusMonths(6).toInstant());
    this.minEndTime = Date.from(endTime.toInstant());
    init();
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

  private static com.atlassian.jira.rest.client.api.domain.Issue log(com.atlassian.jira.rest.client.api.domain.Issue issue) {
    LOGGER.info("{}: {}. {} - {} - {}.", issue.getKey(), issue.getSummary(), DF.format(issue.getCreationDate().toDate()),
    issue.getUpdateDate() != null ? DF.format(issue.getUpdateDate().toDate()) : null,
    issue.getAssignee());
    return issue;
  }

  public void init() {
  }

  public void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectIssues();
  }

  public Map<String, Set<Issue>> collectIssues() {
    return streamIssues().collect(Collectors.groupingBy(Issue::getAssignee, Collectors.toSet()));
  }

  public Stream<Issue> streamIssues() {
    long total = repositories.stream().count();

    return repositories.<Repository>stream()
    .distinct()
    .sorted()
//    .map(status.<String>log(total, "Collecting issues from repository %s."))
    .flatMap(i -> teamIssueStream(i));
  }

  private Stream<Issue> teamIssueStream(String repository) {
    synchronized (client) {
      LOGGER.info("Getting issues for repository {} and users {}", repository, users);
      return StreamSupport.stream(
      client.getSearchClient().searchJql("project = " + repository + " and updatedDate >= '" + DF.format(minStartTime) + "'").claim().getIssues()
      .spliterator(), false).peek(i -> log(i))
      .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getName()))
      .map(i -> Issue.create(repository, i)).filter(i -> i.isActiveDuring(minStartTime, minEndTime))
      .map(JiraIssueCollector::log);
    }
  }

  public Set<String> getUsers() {
    return users;
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
