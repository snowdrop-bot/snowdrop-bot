package io.snowdrop.github.reporting;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Subscriber;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.annotations.Channel;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.OnOverflow;
import io.smallrye.reactive.messaging.annotations.Stream;
import io.snowdrop.Status;
import io.snowdrop.StatusLogger;

@ApplicationScoped
public class GithubReportingFactory {

  @ConfigProperty(name = "github.users")
  Set<String> users;

  @ConfigProperty(name = "github.reporting.organizations")
  Set<String> organizations;

  @ConfigProperty(name = "github.reporting.day-of-week", defaultValue = "4")
  int reportingDayOfWeek;

  @ConfigProperty(name = "github.reporting.hours", defaultValue = "12")
  int reportingHours;

  @Inject
  GitHubClient client;

  @Inject
  @Channel("issues")
  @OnOverflow(value=OnOverflow.Strategy.BUFFER, bufferSize = 100)
  Emitter<Status> issues;

  @Inject
  @Channel("prs")
  @OnOverflow(value=OnOverflow.Strategy.BUFFER, bufferSize = 100)
  Emitter<Status> pullRequests;

  @Produces
  public RepositoryCollector createRepositoryCollector() {
    return new RepositoryCollector(client, users, organizations);
  }

  @Produces
  public IssueCollector createIssueCollector() {
    return new IssueCollector(client, new StatusLogger("issues", issues), reportingDayOfWeek, reportingHours, users, organizations);
  }

  @Produces
  public PullRequestCollector createPullRequestCollector() {
    return new PullRequestCollector(client, new StatusLogger("pull-requests", pullRequests), reportingDayOfWeek, reportingHours, users, organizations);
  }
}
