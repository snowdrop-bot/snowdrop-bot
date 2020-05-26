package io.snowdrop.jira;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class JiraClientFactory {

  @ConfigProperty(name = "jira.url")
  String url;

  @ConfigProperty(name = "jira.username")
  String username;

  @ConfigProperty(name = "jira.password")
  String password;

  @Produces
  public JiraRestClient createClient() {
    AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
    return factory.createWithBasicHttpAuthentication(URI.create(url), username, password);
  }
}
