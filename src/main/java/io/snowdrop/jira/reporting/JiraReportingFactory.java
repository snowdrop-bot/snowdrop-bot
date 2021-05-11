package io.snowdrop.jira.reporting;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.JiraRestClient;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class JiraReportingFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraReportingFactory.class);

  @ConfigProperty(name = "jira.users", defaultValue = "snowdrop-bot")
  Set<String> users;

  @ConfigProperty(name = "jira.reporting.repos")
  Set<String> repositories;

  @ConfigProperty(name = "github.reporting.day-of-week", defaultValue = "4")
  int reportingDayOfWeek;

  @ConfigProperty(name = "github.reporting.hours", defaultValue = "12")
  int reportingHours;

  @Inject
  JiraRestClient client;

  @Produces
  public JiraIssueCollector createIssueCollector() {
    LOGGER.info(getClass().getName() + "#createIssueCollector()...");
    LOGGER.info(getClass().getName() + " client: " + client);
    LOGGER.info(getClass().getName() + " reportingDayOfWeek: " + reportingDayOfWeek);
    LOGGER.info(getClass().getName() + " reportingHours: " + reportingHours);
    LOGGER.info(getClass().getName() + " client: " + users);
    //    new StatusLogger("issues", issues)
    return new JiraIssueCollector(client, null, reportingDayOfWeek, reportingHours, users,repositories);

  }

}
