package io.snowdrop.github.issues;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.snowdrop.github.Github;

@ApplicationScoped
public class GithubIssueBridgeFactory {

  @ConfigProperty(name="github.users")
  Set<String> users;

  @Inject
  BridgeConfig bridgeConfig;

  @Produces
  public List<GithubIssueBridge> createIssueBridge(GitHubClient client) {
    return bridgeConfig.getSourceRepos()
      .stream()
      .map(r -> new GithubIssueBridge(client, r, bridgeConfig.getTargetOrganization() + "/" + Github.repo(r),
                                      bridgeConfig.getAutoLabel().getName(), bridgeConfig.getAutoLabel().getDescription(), bridgeConfig.getAutoLabel().getColor(),
                                      bridgeConfig.getTerminalLabel().getName(), bridgeConfig.getTerminalLabel().getDescription(), bridgeConfig.getTerminalLabel().getColor(),
                                      users))
      .collect(Collectors.toList());
  }

}
