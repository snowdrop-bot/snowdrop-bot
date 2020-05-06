package io.snowdrop.github.reporting;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.reactivex.Flowable;
import io.snowdrop.Status;
import io.snowdrop.github.reporting.model.ForkId;
import io.snowdrop.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

@Singleton
public class GithubReportingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GithubReportingService.class);

  private AtomicReference<Status> forkStatus = new AtomicReference<>(new Status("forkStatus", 100, ""));
  private AtomicReference<Status> repositoryStatus = new AtomicReference<>(new Status("repositories", 100, ""));

  private AtomicReference<Status> issueStatus = new AtomicReference<>(new Status("issues", 100, ""));
  private AtomicReference<Status> pullRequestStatus = new AtomicReference<>(new Status("pull requests", 100, ""));

  @Incoming("forks")
  public void onForkStatus(Status s) {
    forkStatus.set(s);
  }

  @Incoming("repositories")
  public void onRepositoryStatus(Status s) {
    repositoryStatus.set(s);
  }

  @Incoming("issues")
  public void onIssueStatus(Status s) {
    issueStatus.set(s);
  }

  @Incoming("prs")
  public void onPullRequestStatus(Status s) {
    pullRequestStatus.set(s);
  }

  public Publisher<Status> getForkStatuses() {
    return Flowable.interval(1, TimeUnit.SECONDS).map(t -> forkStatus.get()).filter(s -> s != null);
  }

  public Publisher<Status> getRepositoryStatuses() {
    return Flowable.interval(1, TimeUnit.SECONDS).map(t -> repositoryStatus.get()).filter(s -> s != null);
  }

  public Publisher<Status> getIssueStatuses() {
    return Flowable.interval(1, TimeUnit.SECONDS).map(t -> issueStatus.get()).filter(s -> s != null);
  }

  public Publisher<Status> getPullrequests() {
    return Flowable.interval(1, TimeUnit.SECONDS).map(t -> pullRequestStatus.get()).filter(s -> s != null);
  }

  @Inject
  RepositoryCollector repositoryCollector;

  @Inject
  IssueCollector issueCollector;

  @Inject
  PullRequestCollector pullRequestCollector;

  @ConfigProperty(name = "github.reporting.enabled", defaultValue = "false")
  private boolean enabled;

  public void enable() {
    this.enabled = true;
  }

  public void disable() {
    this.enabled = false;
  }

  public boolean status() {
    return enabled;
  }

  @ActivateRequestContext
  void onStart(@Observes StartupEvent ev) {
  }

  @Scheduled(every = "3h")
  public void executeIfEnabled() {
    if (enabled) {
      execute();
    }
  }

  public void execute() {
    repositoryCollector.streamForks().forEach(e -> persist(e));
    repositoryCollector.streamRepositories().forEach(e -> persist(e));
    issueCollector.streamIssues().forEach(e -> persist(e));
    pullRequestCollector.streamPullRequests().forEach(e -> persist(e));
  }

  public void collectAllRepositories() {
    repositoryCollector.streamForks().forEach(e -> persist(e));
    repositoryCollector.streamRepositories().forEach(e -> persist(e));
  }

  public void collectIssues() {
    if (repositoryCollector.getAllRepositories().count() == 0) {
      repositoryCollector.streamForks().forEach(e -> persist(e));
      repositoryCollector.streamRepositories().forEach(e -> persist(e));
    }
    issueCollector.streamIssues().forEach(e -> persist(e));
  }

  public void collectPullRequests() {
    if (repositoryCollector.getAllRepositories().count() == 0) {
      repositoryCollector.streamForks().forEach(e -> persist(e));
      repositoryCollector.streamRepositories().forEach(e -> persist(e));
    }
    pullRequestCollector.streamPullRequests().forEach(e -> persist(e));
  }

  @Transactional
  public void persist(Repository repo) {
    try {
      Repository existing = Repository.findById(new ForkId(repo.getUrl(), repo.getParent()));
      if (existing != null) {
        existing.delete();
      }
      repo.persist();
    } catch (Exception e) {
      LOGGER.error("Failed to persist issue.", e);
    }
  }

  @Transactional
  public void persist(Issue issue) {
    try {
      Issue existing = Issue.findById(issue.getUrl());
      if (existing != null) {
        existing.delete();
      }
      issue.persist();
    } catch (Exception e) {
      LOGGER.error("Failed to persist issue.", e);
    }
  }

  @Transactional
  public void persist(PullRequest pr) {
    try {
      PullRequest existing = PullRequest.findById(pr.getUrl());
      if (existing != null) {
        existing.delete();
      }
      pr.persist();
    } catch (Exception e) {
      LOGGER.error("Failed to persist pull request.", e);
    }
  }

  public RepositoryCollector getRepositoryCollector() {
    return repositoryCollector;
  }

  public IssueCollector getIssueCollector() {
    return issueCollector;
  }

  public PullRequestCollector getPullRequestCollector() {
    return pullRequestCollector;
  }

}
