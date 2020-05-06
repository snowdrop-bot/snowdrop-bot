package io.snowdrop.google;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.GithubReportingService;
import io.snowdrop.reporting.model.Issue;
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
      Set<PullRequest> pullRequests = service.getPullRequestCollector().getPullRequests().filter(p -> p.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());
      Set<Issue> issues = service.getIssueCollector().getIssues().filter(i -> i.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());

      Set<RepositoryWork> workItems = service.getRepositoryCollector().getUsers().stream().flatMap(u -> {
          Map<String, Set<Issue>> userIssues = issues.stream().filter(i -> u.equals(i.getAssignee())).collect(Collectors.groupingBy(i ->  i.getRepository(), Collectors.toSet()));
          Map<String, Set<PullRequest>> userPullRequests = pullRequests.stream().filter(p -> u.equals(p.getCreator())).collect(Collectors.groupingBy(p ->  p.getRepository(), Collectors.toSet()));
          return Stream.concat(userIssues.keySet().stream(),  userPullRequests.keySet().stream())
            .distinct()
            .map(r -> new RepositoryWork(Github.user(r), Github.repo(r), u,
                                         userIssues.getOrDefault(r, Collections.emptySet()),
                                         userPullRequests.getOrDefault(r, Collections.emptySet())));
        }).collect(Collectors.toSet());

      List<Request> prs = createRequests(startTime, endTime, workItems);
      BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(prs);
      BatchUpdateDocumentResponse response = docs.documents().batchUpdate(reportDocumentId, body).execute();
    } catch (IOException e) {
      throw BotException.launderThrowable(e);
    }
  }

  public List<Request> createRequests(Date startTime, Date endTime, Set<RepositoryWork> items) {
    List<Request> requests = new ArrayList<>();
    ContentBuilder builder = new ContentBuilder();
    builder.newline().bold(DF.format(startTime) + " - " +  DF.format(endTime)).newline();
    items.stream().collect(Collectors.groupingBy(RepositoryWork::getWorker, Collectors.toSet())).entrySet().forEach(e -> {
      final StringBuilder sb = new StringBuilder();
      String user = e.getKey();
      Set<RepositoryWork> work = e.getValue();
      builder.bold(user).newline();
      work.stream().forEach(w  -> {
          builder.write(w.getName() + " [GREEN]:").newline();
          Set<Issue> issues = w.getIssues();
          if (issues.size() > 0) {
          builder.tab(1).write("Tasks").newline();
          w.getIssues().forEach(i -> {
                  builder.tab(2).link("(#" +  i.getNumber() + "): " +  i.getTitle(), i.getUrl()).newline();
                  w.getLinkedRequests(i.getNumber()).forEach(p  -> {
                      builder.tab(3).link("(#" +  p.getNumber() + "): " +  p.getTitle(), p.getUrl()).newline();
                    });
            });
          }

          Set<PullRequest> otherPullRequests = w.getUnLinkedRequests();
          if (otherPullRequests.size() > 0) {
            builder.tab(1).write("Other Pull Requests").newline();
            otherPullRequests.stream().forEach(p -> {
                builder.tab(2).link("(#" +  p.getNumber() + "): " +  p.getTitle(), p.getUrl()).newline();
              });
          }
      });
      builder.newline();
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
