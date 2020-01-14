package io.snowdrop.github.issues;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class GithubIssueBridgeFactory {

  @ConfigProperty(name="github.source.repo")
  String sourceRepository;

  @ConfigProperty(name="github.target.repo")
  String targetRepository;

  @ConfigProperty(name="github.target.terminal.label")
  String terminalLabel;

  @ConfigProperty(name="github.users")
  Set<String> users;

  @Produces
  public GithubIssueBridge createIssueBridge(GitHubClient client, IssueService issueService, LabelService labelService) {
    return new GithubIssueBridge(client, issueService, labelService, sourceRepository, targetRepository, terminalLabel, users);
  }

}
