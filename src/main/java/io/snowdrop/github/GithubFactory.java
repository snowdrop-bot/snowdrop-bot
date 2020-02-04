package io.snowdrop.github;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GithubFactory {

  @ConfigProperty(name="github.token")
  String token;

  @Produces
  public GitHubClient createClient() {
    GitHubClient client = new GithubRateLimitedClient();
    client.setOAuth2Token(token);
    return client;
  }

}
