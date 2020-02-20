package io.snowdrop.google;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;

public class RepositoryWork {

  private final String owner;
  private final String name;
  private final String worker;
  private final Set<Issue> issues;
  private final Set<PullRequest> pullRequests;

  public RepositoryWork(String owner,String name, String worker, Set<Issue> issues, Set<PullRequest> pullRequests) {
    if (issues == null) {
      throw new IllegalArgumentException("Issues cannot be null.");
    }
    if (pullRequests == null) {
      throw new IllegalArgumentException("Pull requests cannot be null.");
    }
    this.name = name;
    this.owner = owner;
    this.worker = worker;
    this.issues = issues;
    this.pullRequests = pullRequests;
  }

  public String getName() {
    return name;
  }

  public String getOwner() {
    return this.owner;
  }

  public String getWorker() {
    return this.worker;
  }

 public Set<Issue> getIssues() {
    return issues;
  }

  public Set<PullRequest> getPullRequests() {
    return pullRequests;
  }


  public Set<PullRequest> getLinkedRequests(int issueNumber) {
    return pullRequests != null ? pullRequests.stream().filter(p -> p.getIssues().contains(issueNumber)).collect(Collectors.toSet()) : Collections.emptySet();
  }
}
