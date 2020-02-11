package io.snowdrop.google;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.BatchUpdateDocumentResponse;
import com.google.api.services.docs.v1.model.Request;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.reporting.GithubReportingService;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.google.dsl.ContentBuilder;

@Path("/docs")
public class GoogleDocsEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDocsEndpoint.class);
  private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  @ConfigProperty(name = "google.docs.report.document-id")
  String reportDocumentId;

  @Inject
  Docs docs;

  @Inject
  GithubReportingService service;

  private void populate(Date startTime, Date endTime) {
    try {
      List<Request> prs = createRequests(service.getPullRequestCollector().getPullRequests().filter(p -> p.isActiveDuring(startTime, endTime)).collect(Collectors.toSet()));
      BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(prs);
      BatchUpdateDocumentResponse response = docs.documents().batchUpdate(reportDocumentId, body).execute();
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  public List<Request> createRequests(Set<PullRequest> pullRequests) {
    List<Request> requests = new ArrayList<>();
    ContentBuilder builder = new ContentBuilder();
    pullRequests.stream().collect(Collectors.groupingBy(PullRequest::getCreator, Collectors.toSet())).entrySet().forEach(e -> {
      final StringBuilder sb = new StringBuilder();
      String user = e.getKey();
      Set<PullRequest> prs = e.getValue();
      builder.bold(user).newline().bulletsOn();

      prs.stream()
           .collect(Collectors.groupingBy(PullRequest::getRepository, Collectors.toSet())).entrySet()
          .forEach(r -> {
              builder.tab().write(r.getKey().split("/")[1] + "[GREEN]:").newline();
              r.getValue().stream().forEach(p -> {
                  builder.tab(2).write(p.getTitle()).newline();
                  builder.tab(3).link(p.getUrl()).newline();
            });
          });
      builder.bulletsOff().newline();
    });
    requests.addAll(builder.getAllRequests());
    return requests;
  }

  @GET
  @Path("/generate")
  @Produces(MediaType.TEXT_PLAIN)
  public String createReport(@QueryParam("startTime") String startTimeString, @QueryParam("endTime") String endTimeString) {
    try {
        Date startTime = startTimeString != null ? DF.parse(startTimeString)
                : Date.from(service.getPullRequestCollector().getStartTime().toInstant());
        Date endTime = endTimeString != null ? DF.parse(endTimeString)
                : Date.from(service.getPullRequestCollector().getEndTime().toInstant());
      populate(startTime, endTime);
      return docs.documents().get(reportDocumentId).execute().getTitle();
    } catch (Exception e) {
      throw BotException.launderThrowable(e);
    }
  }
}
