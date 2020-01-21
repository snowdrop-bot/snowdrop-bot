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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

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
  private final Map<String, Set<PullRequest>> pullRequests = new HashMap<>();
  private final Map<String, Set<Issue>> issues = new HashMap<>();

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
        issues.put(u, new HashSet<>());
      });
  }


  public synchronized void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectForks();
    collectIssues();
    collectPullRequests();
  }

  public synchronized Map<String, Set<Repository>> collectForks() {
    users.stream().forEach(u -> {
        LOGGER.info("Getting forks for user: {}.", u);
        Set<Repository> forks = userForks(u);
        repositories.get(u).addAll(forks);
        LOGGER.info("User: {} forks: [{}].", u, forks.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
      });
    return repositories;
    }


    public Map<String, Set<Issue>> collectIssues() {
      repositories.values().stream().flatMap(s -> s.stream()).map(Repository::getParent).filter(r -> r != null).distinct().sorted().forEach(r -> {
          issues.putAll(teamIssues(r, "all"));
        });
      return issues;
    }

  public Map<String, Set<PullRequest>> collectPullRequests() {
    repositories.values().stream().flatMap(s -> s.stream()).map(Repository::getParent).filter(r -> r != null).distinct().sorted().forEach(r -> {
          pullRequests.putAll(teamPullRequests(r, "all"));
        });
      return pullRequests;
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
        .filter(r -> organizations.contains(r.getParent().getOwner().getLogin()))
        .map(Repository::create)
        .collect(Collectors.toSet());
    } catch (final IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private org.eclipse.egit.github.core.Repository repository(String user, String name) {
    try {
      return repositoryService.getRepository(user, name);
    } catch (IOException e) {
      throw BotException.launderThrowable("Error reading repository:" + user + "/" + name, e);
    }
  }

  private Map<String, Set<PullRequest>> teamPullRequests(final String repository, final String state) {
    try {
      LOGGER.info("Getting {} pull requests for repository: {}", state, repository);
      return pullRequestService.getPullRequests(() -> repository, state)
        .stream()
        .filter(p -> users.contains(p.getUser().getLogin()))
        .map(p -> PullRequest.create(repository, p))
        .filter(p -> p.isActiveDuring(minStartTime, minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.groupingBy(PullRequest::getCreator, Collectors.toSet()));
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Set<PullRequest> userPullRequests(final String user, final Repository repository, final String state) {
    try {
      String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
      LOGGER.info("Getting {} pull requests for repository: {}", state, id);
      return pullRequestService.getPullRequests(() -> id, state)
        .stream()
        .filter(p -> p.getUser().getLogin().equals(user))
        .map(p -> PullRequest.create(id, p))
        .filter(i -> i.isActiveDuring(minStartTime, minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Set<Issue> userIssues(String user, Repository repository, String state) {
    try {
      String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
      LOGGER.info("Getting {} issues for repository: {}", state, repository.getName());
      return issueService.getIssues(Github.user(id), Github.repo(id), Github.params().state(state).build())
        .stream()
        .map(i -> Issue.create(id, i))
        .filter(i -> i.getCreatedAt().before(minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Map<String, Set<Issue>> teamIssues(String repository, String state) {
    try {
      LOGGER.info("Getting {} issues for repository: {}", state, repository);
      return issueService.getIssues(Github.user(repository), Github.repo(repository), Github.params().state(state).build())
        .stream()
        .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
        .map(i -> Issue.create(repository, i))
        .filter(i -> i.isActiveDuring(minStartTime, minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.groupingBy(Issue::getCreator, Collectors.toSet()));
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }


  /**
   * Log and return self.
   * Lambda friendly log hack.
   */
  private static Issue log(Issue issue) {
    LOGGER.info("{}: {}",issue.getNumber(),issue.getTitle());
    return issue;
  }

  /**
   * Log and return self.
   * Lambda friendly log hack.
   */
  private static PullRequest log(PullRequest pull) {
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

  public Map<String, Set<PullRequest>> getPullRequests() {
    return pullRequests;
  }

  public Map<String, Set<Issue>> getIssues() {
    return issues;
  }

  public ZonedDateTime getStartTime() {
    return startTime;
  }

  public ZonedDateTime getEndTime() {
    return endTime;
  }
}
