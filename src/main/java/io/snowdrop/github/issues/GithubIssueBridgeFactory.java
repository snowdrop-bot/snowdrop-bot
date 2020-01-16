package io.snowdrop.github.issues;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;

import io.snowdrop.github.Github;

@ApplicationScoped
public class GithubIssueBridgeFactory {

  @Inject
  BridgeConfig bridgeConfig;

  @Produces
  public List<GithubIssueBridge> createIssueBridge(GitHubClient client, IssueService issueService, LabelService labelService) {
    return bridgeConfig.getSourceRepos()
      .stream()
      .map(r -> new GithubIssueBridge(client, issueService, labelService, r, bridgeConfig.getTargetOrganization() + "/" + Github.repo(r), bridgeConfig.getTerminalLabel(), bridgeConfig.getUsers()))
      .collect(Collectors.toList());
  }

}
