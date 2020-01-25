package io.snowdrop.github.reporting;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.snowdrop.github.reporting.model.ForkId;
import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

@Singleton
public class GithubReportingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GithubReportingService.class);

  @Inject
  GithubReporting reporting;

  @ConfigProperty(name = "github.reporting.enabled", defaultValue = "true")
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
    popullateRepos();
    popullateIssues();
    popullatePullRequests();
  }

  @Scheduled(delay=1, delayUnit = TimeUnit.HOURS, every = "3h")
  public void executeIfEnabled() {
    if (enabled) {
      execute();
    }
  }

  public void execute() {
      reporting.collectForks().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
      reporting.collectIssues().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
      reporting.collectPullRequests().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
  }

  public void collectIssues() {
      reporting.collectForks().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
      reporting.collectIssues().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
  }

  public void collectPullRequests() {
      reporting.collectForks().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
      reporting.collectIssues().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
      reporting.collectPullRequests().values().stream().flatMap(Collection::stream).forEach(e -> persist(e));
  }



  @Transactional
  public void persist(Repository repo) {
    Repository existing = Repository.findById(new ForkId(repo.getUrl(), repo.getParent()));
    if (existing != null) {
      existing.delete();
    }
    repo.persist();
  }

  @Transactional
  public void persist(Issue issue) {
    Issue existing = Issue.findById(issue.getUrl());
    if (existing != null) {
      existing.delete();
    }
    issue.persist();
  }

  @Transactional
  public void persist(PullRequest pr) {
    PullRequest existing = PullRequest.findById(pr.getUrl());
    if (existing != null) {
      existing.delete();
    }
    pr.persist();
  }

  @Transactional
  public void popullateRepos() {
    LOGGER.info("Populating repositories.");
    reporting.getRepositories().putAll(Repository.<Repository>streamAll()
        .collect(Collectors.groupingBy(Repository::getOwner, Collectors.toSet())));

  }

  @Transactional
  public void popullateIssues() {
    LOGGER.info("Populating issues.");
    reporting.getIssues().putAll(
        Issue.<Issue>streamAll().collect(Collectors.groupingBy(Issue::getAssignee, Collectors.toSet())));
  }

  @Transactional
  public void popullatePullRequests() {
    LOGGER.info("Populating pull requests.");
    reporting.getPullRequests().putAll(PullRequest.<PullRequest>streamAll()
             .collect(Collectors.groupingBy(PullRequest::getCreator, Collectors.toSet())));
  }

  public GithubReporting getReporting() {
    return reporting;
  }

}
