package io.snowdrop.jira.reporting;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.scheduler.Scheduled;
import io.snowdrop.reporting.IssueReportingService;
import io.snowdrop.reporting.model.Issue;

@Singleton
public class JiraReportingService implements IssueReportingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraReportingService.class);

  @Inject
  JiraIssueCollector issueCollector;

  @ConfigProperty(name = "jira.reporting.enabled", defaultValue = "false")
  private boolean enabled;

  @Scheduled(every = "3h")
  public void executeIfEnabled() {
    if (enabled) {
      execute();
    }
  }

  public void execute() {
    issueCollector.streamIssues().forEach(e -> persist(e));
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

  public void collectIssues() {
    issueCollector.streamIssues().forEach(e -> persist(e));
  }
}
