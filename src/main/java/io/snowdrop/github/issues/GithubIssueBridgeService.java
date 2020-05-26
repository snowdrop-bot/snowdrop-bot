package io.snowdrop.github.issues;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class GithubIssueBridgeService {

  @Inject
  GitHubClient client;

  @Inject
  List<GithubIssueBridge> bridges;

  @ConfigProperty(name = "github.bridge.enabled", defaultValue = "false")
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

  @Scheduled(delay = 1, delayUnit = TimeUnit.HOURS, every = "3h")
  public void executeIfEnabled() {
    if (enabled) {
      bridgeNow();
    }
  }

  public void bridgeNow() {
    bridges.forEach(b -> {
        b.refresh();
        b.assignTeamIssues();
        b.labelTeamIssues();
        b.closeTeamIssues();
        b.cloneTeamIssues();
      });
  }

  public List<GithubIssueBridge> getBridges() {
   return bridges;
  }

}
