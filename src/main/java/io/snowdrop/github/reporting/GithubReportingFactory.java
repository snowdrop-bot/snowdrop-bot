package io.snowdrop.github.reporting;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.snowdrop.github.Github;

@ApplicationScoped
public class GithubReportingFactory {

  @ConfigProperty(name = "github.users")
  Set<String> users;

  @ConfigProperty(name = "github.reporting.organizations")
  Set<String> organizations;

  @ConfigProperty(name = "github.reporting.day-of-week", defaultValue = "4")
  int reportingDayOfWeek;

  @ConfigProperty(name = "github.reporting.hours", defaultValue = "12")
  int reportingHours;

  @Inject
  PullRequestService pullRequestService;

  @Inject
  RepositoryService repositoryService;

  @Inject
  IssueService issueService;


  @Produces
  public GithubReporting createGithubReporting() {
    return new GithubReporting(repositoryService, pullRequestService, issueService, reportingDayOfWeek, reportingHours, users, organizations);
  }

}
