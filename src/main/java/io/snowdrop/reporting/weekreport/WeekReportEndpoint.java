package io.snowdrop.reporting.weekreport;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.GithubReportingService;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;
import io.snowdrop.google.RepositoryWork;
import io.snowdrop.reporting.model.Issue;

/**
 * <p>Generates the weekly report.</p>
 */
@Path("/weeklyreport")
public class WeekReportEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeekReportEndpoint.class);
    private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

    @ConfigProperty(name = "github.users")
    Set<String> users;

    //  @ConfigProperty(name = "google.docs.report.document-id")
    //  String reportDocumentId;

    //  @Inject
    //  Docs docs;

    @Inject
    GithubReportingService service;

    private void populate(Date startTime, Date endTime) {
        //    try {
        Set<PullRequest> pullRequests = service.getPullRequestCollector().getPullRequests().filter(p -> p.isActiveDuring(startTime, endTime))
                .collect(Collectors.toSet());
        Set<Issue> issues = service.getIssueCollector().getIssues().filter(i -> i.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());

        Set<RepositoryWork> workItems = service.getRepositoryCollector().getUsers().stream().flatMap(u -> {
            Map<String, Set<Issue>> userIssues = issues.stream().filter(i -> u.equals(i.getAssignee()))
                    .collect(Collectors.groupingBy(i -> i.getRepository(), Collectors.toSet()));
            Map<String, Set<PullRequest>> userPullRequests = pullRequests.stream().filter(p -> u.equals(p.getCreator()))
                    .collect(Collectors.groupingBy(p -> p.getRepository(), Collectors.toSet()));
            return Stream.concat(userIssues.keySet().stream(), userPullRequests.keySet().stream())
                    .distinct()
                    .map(r -> new RepositoryWork(Github.user(r), Github.repo(r), u,
                            userIssues.getOrDefault(r, Collections.emptySet()),
                            userPullRequests.getOrDefault(r, Collections.emptySet())));
        }).collect(Collectors.toSet());

        //      List<Request> prs = createRequests(startTime, endTime, workItems);
        //      BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(prs);
        //      BatchUpdateDocumentResponse response = docs.documents().batchUpdate(reportDocumentId, body).execute();
        //    } catch (IOException e) {
        //      throw BotException.launderThrowable(e);
        //    }
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
            LOGGER.info("Time: " + startTimeString + "," + endTimeString);
            LOGGER.info("Time: " + startTime + "," + endTime);
            populate(startTime, endTime);
            String mdText = DevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info(mdText);
            mdText = WeeklyDevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info(mdText);
            return mdText;
        } catch (Exception e) {
            throw BotException.launderThrowable(e);
        }
    }

}
