package io.snowdrop.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

  @ConfigProperty(name = "google.docs.report.document-id")
  String reportDocumentId;

  @Inject
  Docs docs;

  @Inject
  GithubReportingService reporting;

  private void populate() {
    try {
      List<Request> prs = createRequests(reporting.getReporting().getPullRequests());
      BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(prs);
      BatchUpdateDocumentResponse response = docs.documents().batchUpdate(reportDocumentId, body).execute();
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  public List<Request> createRequests(Map<String, Set<PullRequest>> pullRequests) {
    List<Request> requests = new ArrayList<>();
    ContentBuilder builder = new ContentBuilder();
    pullRequests.entrySet().forEach(e -> {
      final StringBuilder sb = new StringBuilder();
      String user = e.getKey();
      Set<PullRequest> prs = e.getValue();
      builder.bold(user).newline().bulletsOn();

      prs.stream().collect(Collectors.groupingBy(PullRequest::getRepository, Collectors.toSet())).entrySet()
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
  public String createReport() {
    try {
      populate();
      return docs.documents().get(reportDocumentId).execute().getTitle();
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }
}
