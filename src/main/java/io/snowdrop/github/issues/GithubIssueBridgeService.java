package io.snowdrop.github.issues;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
  List<GithubIssueBridge> bridges;

  @ConfigProperty(name = "github.bridge.enabled", defaultValue = "true")
  private boolean enabled;

  public void enable() {
    this.enabled = true;
  }

  public void disable() {
    this.enabled = false;
  }

  public Boolean status() {
    return enabled;
  }

  @Scheduled(every = "1h")
  public void executeIfEnabled() {
    if (enabled) {
      bridgeNow();
    }
  }

  public void bridgeNow() {
    bridges.forEach(b -> {
        b.refresh();
        b.assignTeamIssues();
        b.closeTeamIssues();
        b.cloneTeamIssues();
      });
  }

}
