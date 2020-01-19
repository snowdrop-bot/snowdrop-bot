/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
**/

package io.snowdrop.github.reporting;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.IssueDTO;
import io.snowdrop.github.reporting.model.PullRequestDTO;

public class GithubReporting {

  private static final Logger LOGGER = LoggerFactory.getLogger(GithubReporting.class);

  private final RepositoryService repositoryService;
  private final PullRequestService pullRequestService;
  private final IssueService issueService;
  private final int reportingDay;
  private final int reportingHour;

  private final Set<String> users;
  private final Set<String> organizations;

  //These dates represent current reporting period
  private final ZonedDateTime startTime;
  private final ZonedDateTime endTime;

  //These represent the oldest reporting period possible
  private final Date minStartTime;
  private final Date minEndTime;

  private final Map<String, Set<Repository>> repositories = new HashMap<>();

  private final Map<String, Set<PullRequestDTO>> openPullRequests = new HashMap<>();
  private final Map<String, Set<PullRequestDTO>> closedPullRequests = new HashMap<>();
  private final Map<String, Set<PullRequestDTO>> pullRequests = new HashMap<>();

  private final Map<String, Set<IssueDTO>> openIssues = new HashMap<>();
  private final Map<String, Set<IssueDTO>> closedIssues = new HashMap<>();
  private final Map<String, Set<IssueDTO>> issues = new HashMap<>();

  public GithubReporting(RepositoryService repositoryService, PullRequestService pullRequestService,
      IssueService issueService, int reportingDay, int reportingHour, Set<String> users, Set<String> organizations) {
    this.repositoryService = repositoryService;
    this.pullRequestService = pullRequestService;
    this.issueService = issueService;
    this.reportingDay = reportingDay;
    this.reportingHour = reportingHour;
    this.users = users;
    this.organizations = organizations;

    this.endTime = ZonedDateTime.now().with(DayOfWeek.of(reportingDay)).withHour(reportingHour);
    this.startTime = endTime.minusWeeks(1);

    this.minStartTime = Date.from(startTime.minusMonths(1).toInstant());
    this.minEndTime = Date.from(endTime.minusMonths(1).toInstant());
    init();
  }

  public void init() {
    users.stream().forEach(u -> {
      repositories.put(u, new HashSet<>());

      pullRequests.put(u, new HashSet<>());
      openPullRequests.put(u, new HashSet<>());
      closedPullRequests.put(u, new HashSet<>());

      issues.put(u, new HashSet<>());
      openIssues.put(u, new HashSet<>());
      closedIssues.put(u, new HashSet<>());
    });
  }

  public synchronized void refresh() {
    LOGGER.info("Refreshing report data.");
    users.stream().forEach(u -> {
      LOGGER.info("Getting forks for user: {}.", u);
      Set<Repository> forks = userForks(u);
      repositories.get(u).addAll(forks);
      LOGGER.info("User: {} forks: [{}].", u, forks.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
    });

    repositories.values().stream().flatMap(s -> s.stream()).distinct().forEach(r -> {
        openPullRequests.putAll(teamPullRequests(r, "open"));
        pullRequests.putAll(openPullRequests);
        closedPullRequests.putAll(teamPullRequests(r, "closed"));
        pullRequests.putAll(closedPullRequests);

        openIssues.putAll(teamIssues(r, "open"));
        issues.putAll(openIssues);
        closedIssues.putAll(teamIssues(r, "closed"));
        issues.putAll(closedIssues);
    });


    /*
     * Works but not the most efficient approach
     *
    repositories.entrySet().stream().forEach(e -> {
      e.getValue().stream().forEach(r -> {

        LOGGER.info("Getting pull requests for user: {}.", e.getKey());
        openPullRequests.get(e.getKey()).addAll(userPullRequests(e.getKey(), r, "open"));
        closedPullRequests.get(e.getKey()).addAll(userPullRequests(e.getKey(), r, "closed"));
        pullRequests.get(e.getKey()).addAll(openPullRequests.get(e.getKey()));
        pullRequests.get(e.getKey()).addAll(closedPullRequests.get(e.getKey()));

        LOGGER.info("Getting issues for user: {}.", e.getKey());
        openIssues.get(e.getKey()).addAll(userIssues(e.getKey(), r, "open"));
        closedIssues.get(e.getKey()).addAll(userIssues(e.getKey(), r, "closed"));
        issues.get(e.getKey()).addAll(openIssues.get(e.getKey()));
        issues.get(e.getKey()).addAll(closedIssues.get(e.getKey()));
      });
    });
    */
  }

  /**
   * Get all the repositories of the specified user.
   *
   * @param user The user
   * @return A set of {@link Repository}.
   */
  public Set<Repository> userForks(final String user) {
    try {
      return repositoryService.getRepositories(user).stream().filter(r -> r.isFork())
          .map(r -> repository(user, r.getName()))
          .filter(r -> organizations.contains(r.getParent().getOwner().getLogin())).collect(Collectors.toSet());
    } catch (final IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Repository repository(String user, String name) {
    try {
      return repositoryService.getRepository(user, name);
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Map<String, Set<PullRequestDTO>> teamPullRequests(final Repository repository, final String state) {
    final Repository actual = repository.isFork() ? repository.getParent() : repository;
    try {
      String id = actual.getOwner().getLogin() + "/" + actual.getName();
      LOGGER.info("Getting {} pull requests for repository: {}", state, id);
      return pullRequestService.getPullRequests(() -> id, state)
            .stream()
            .filter(p -> users.contains(p.getUser().getLogin()))
            .map(p -> PullRequestDTO.create(id, p))
            .filter(p -> p.isActiveDuring(minStartTime, minEndTime))
            .map(GithubReporting::log)
            .collect(Collectors.groupingBy(PullRequestDTO::getCreator, Collectors.toSet()));
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Set<PullRequestDTO> userPullRequests(final String user, final Repository repository, final String state) {
    final Repository actual = repository.isFork() ? repository.getParent() : repository;
    try {
      String id = actual.getOwner().getLogin() + "/" + actual.getName();
      LOGGER.info("Getting {} pull requests for repository: {}", state, id);
      return pullRequestService.getPullRequests(() -> id, state)
            .stream()
            .filter(p -> p.getUser().getLogin().equals(user))
            .map(p -> PullRequestDTO.create(id, p))
            .filter(i -> i.isActiveDuring(minStartTime, minEndTime))
            .map(GithubReporting::log)
            .collect(Collectors.toSet());
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Set<IssueDTO> userIssues(String user, Repository repository, String state) {
    final Repository actual = repository.isFork() ? repository.getParent() : repository;
    try {
      String id = actual.getOwner().getLogin() + "/" + actual.getName();
      LOGGER.info("Getting {} issues for repository: {}", state, repository.getName());
      return issueService.getIssues(actual.getOwner().getLogin(), actual.getName(), Github.params().state(state).build())
          .stream()
          .map(i -> IssueDTO.create(id, i))
          .filter(i -> i.getCreatedAt().before(minEndTime))
          .map(GithubReporting::log)
          .collect(Collectors.toSet());
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Map<String, Set<IssueDTO>> teamIssues(Repository repository, String state) {
    final Repository actual = repository.isFork() ? repository.getParent() : repository;
    try {
      String id = actual.getOwner().getLogin() + "/" + actual.getName();
      LOGGER.info("Getting {} issues for repository: {}", state, repository.getName());
      return issueService.getIssues(actual.getOwner().getLogin(), actual.getName(), Github.params().state(state).build())
          .stream()
          .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
          .map(i -> IssueDTO.create(id, i))
          .filter(i -> i.isActiveDuring(minStartTime, minEndTime))
          .map(GithubReporting::log)
          .collect(Collectors.groupingBy(IssueDTO::getCreator, Collectors.toSet()));
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }


  /**
   * Log and return self.
   * Lambda friendly log hack.
   */
  private static IssueDTO log(IssueDTO issue) {
    LOGGER.info("{}: {}",issue.getNumber(),issue.getTitle());
    return issue;
  }

  /**
   * Log and return self.
   * Lambda friendly log hack.
   */
  private static PullRequestDTO log(PullRequestDTO pull) {
    LOGGER.info("{}: {}",pull.getNumber(),pull.getTitle());
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

  public Map<String, Set<PullRequestDTO>> getOpenPullRequests() {
    return openPullRequests;
  }

  public Map<String, Set<PullRequestDTO>> getClosedPullRequests() {
    return closedPullRequests;
  }

  public Map<String, Set<IssueDTO>> getOpenIssues() {
    return openIssues;
  }

  public Map<String, Set<IssueDTO>> getClosedIssues() {
    return closedIssues;
  }

  public Map<String, Set<PullRequestDTO>> getPullRequests() {
    return pullRequests;
  }

  public Map<String, Set<IssueDTO>> getIssues() {
    return issues;
  }

  public ZonedDateTime getStartTime() {
    return startTime;
  }

  public ZonedDateTime getEndTime() {
    return endTime;
  }
}
