package io.snowdrop.github.issues;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.UserService;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class GithubIssueBridgeService {

  @Inject
  GitHubClient client;
  
  @Inject
  IssueService issueService;

  @Inject
  LabelService labelService;
  
  @Inject
  UserService userService;

  @Inject
  GithubIssueBridge bridge;
  
  @Scheduled(every="10m")
  void minutely() {
    bridge.refresh();
    bridge.assignTeamIssues();
    bridge.closeTeamIssues();
    bridge.cloneTeamIssues();
  }

}

