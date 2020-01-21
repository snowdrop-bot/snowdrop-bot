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

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.IssueDTO;
import io.snowdrop.github.reporting.model.PullRequestDTO;
import io.snowdrop.github.reporting.model.RepositoryDTO;

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

  private final Map<String, Set<RepositoryDTO>> repositories = new HashMap<>();
  private final Map<String, Set<PullRequestDTO>> pullRequests = new HashMap<>();
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
        issues.put(u, new HashSet<>());
      });
  }


  public synchronized void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectForks();
    collectIssues();
    collectPullRequests();
  }

  public synchronized Map<String, Set<RepositoryDTO>> collectForks() {
    users.stream().forEach(u -> {
        LOGGER.info("Getting forks for user: {}.", u);
        Set<RepositoryDTO> forks = userForks(u);
        repositories.get(u).addAll(forks);
        LOGGER.info("User: {} forks: [{}].", u, forks.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
      });
    return repositories;
    }


    public Map<String, Set<IssueDTO>> collectIssues() {
      repositories.values().stream().flatMap(s -> s.stream()).map(RepositoryDTO::getParent).filter(r -> r != null).distinct().sorted().forEach(r -> {
          issues.putAll(teamIssues(r, "all"));
        });
      return issues;
    }

  public Map<String, Set<PullRequestDTO>> collectPullRequests() {
    repositories.values().stream().flatMap(s -> s.stream()).map(RepositoryDTO::getParent).filter(r -> r != null).distinct().sorted().forEach(r -> {
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
  public Set<RepositoryDTO> userForks(final String user) {
    try {
      return repositoryService.getRepositories(user).stream().filter(r -> r.isFork())
        .map(r -> repository(user, r.getName()))
        .filter(r -> organizations.contains(r.getParent().getOwner().getLogin()))
        .map(RepositoryDTO::create)
        .collect(Collectors.toSet());
    } catch (final IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Repository repository(String user, String name) {
    try {
      return repositoryService.getRepository(user, name);
    } catch (IOException e) {
      throw BotException.launderThrowable("Error reading repository:" + user + "/" + name, e);
    }
  }

  private Map<String, Set<PullRequestDTO>> teamPullRequests(final String repository, final String state) {
    try {
      LOGGER.info("Getting {} pull requests for repository: {}", state, repository);
      return pullRequestService.getPullRequests(() -> repository, state)
        .stream()
        .filter(p -> users.contains(p.getUser().getLogin()))
        .map(p -> PullRequestDTO.create(repository, p))
        .filter(p -> p.isActiveDuring(minStartTime, minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.groupingBy(PullRequestDTO::getCreator, Collectors.toSet()));
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Set<PullRequestDTO> userPullRequests(final String user, final RepositoryDTO repository, final String state) {
    try {
      String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
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

  private Set<IssueDTO> userIssues(String user, RepositoryDTO repository, String state) {
    try {
      String id = repository.isFork() ? repository.getParent() : repository.getOwner() + "/" + repository.getName();
      LOGGER.info("Getting {} issues for repository: {}", state, repository.getName());
      return issueService.getIssues(Github.user(id), Github.repo(id), Github.params().state(state).build())
        .stream()
        .map(i -> IssueDTO.create(id, i))
        .filter(i -> i.getCreatedAt().before(minEndTime))
        .map(GithubReporting::log)
        .collect(Collectors.toSet());
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  private Map<String, Set<IssueDTO>> teamIssues(String repository, String state) {
    try {
      LOGGER.info("Getting {} issues for repository: {}", state, repository);
      return issueService.getIssues(Github.user(repository), Github.repo(repository), Github.params().state(state).build())
        .stream()
        .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
        .map(i -> IssueDTO.create(repository, i))
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

  public Map<String, Set<RepositoryDTO>> getRepositories() {
    return repositories;
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
