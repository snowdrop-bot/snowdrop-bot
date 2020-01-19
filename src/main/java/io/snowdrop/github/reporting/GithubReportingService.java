package io.snowdrop.github.reporting;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.scheduler.Scheduled;

@Singleton
public class GithubReportingService {

  @Inject
  GithubReporting reporting;

  @Scheduled(every = "1h")
  public void hourly() {
    reporting.refresh();
  }

  public GithubReporting getReporting() {
    return reporting;
  }

}
