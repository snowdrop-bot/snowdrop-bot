package io.snowdrop.github;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.egit.github.core.client.GitHubClient;
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
