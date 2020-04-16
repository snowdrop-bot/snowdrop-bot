
package io.snowdrop.jira;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlassian.util.concurrent.Promise;
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class JiraService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JiraService.class);

  @Inject
  JiraRestClient client;


  @Scheduled(every = "3h")
  public void getProject() {
    Promise<Project> project = client.getProjectClient().getProject("ENTSBT");
    project.done(p ->  {
        LOGGER.info("Connected to JIRA project:" + p.getName());
    });
  }
}
