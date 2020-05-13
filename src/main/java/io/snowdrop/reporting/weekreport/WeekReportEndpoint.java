package io.snowdrop.reporting.weekreport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
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

import com.google.common.collect.ImmutableMap;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
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
    private static final SimpleDateFormat WEEK_YEAR_FORMAT = new SimpleDateFormat("w");

    @ConfigProperty(name = "github.users")
    Set<String> users;

    //  @ConfigProperty(name = "google.docs.report.document-id")
    //  String reportDocumentId;

    //  @Inject
    //  Docs docs;

    @Inject
    GitHubClient client;

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
    }

    @GET
    @Path("/generate")
    @Produces(MediaType.TEXT_PLAIN)
    public String createReport(@QueryParam("startTime") String startTimeString, @QueryParam("endTime") String endTimeString) {
        String mdText;
        String weekNumber;
        String issueTitle;
        List<Label> lstLabels = new LinkedList() {{
            new Label().setName("report");
        }};
        org.eclipse.egit.github.core.Issue issue;
        IssueService issueService = new IssueService(client);
        List<org.eclipse.egit.github.core.Issue> lstIssue;
        try {
            Date startTime = startTimeString != null ? DF.parse(startTimeString)
                    : Date.from(service.getPullRequestCollector().getStartTime().toInstant());
            Date endTime = endTimeString != null ? DF.parse(endTimeString)
                    : Date.from(service.getPullRequestCollector().getEndTime().toInstant());
            LOGGER.info("Time: " + startTimeString + "," + endTimeString);
            LOGGER.info("Time: " + startTime + "," + endTime);
            weekNumber = WEEK_YEAR_FORMAT.format(endTime);
            LOGGER.info("weekNumber: " + weekNumber);
            populate(startTime, endTime);
            mdText = WeeklyDevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info(mdText);
            updateGithubIssue(mdText, "week_" + weekNumber, lstLabels, issueService, "snowdrop", "snowdrop-team");
            mdText = DevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info(mdText);
            updateGithubIssue(mdText, "dev_week_" + weekNumber, lstLabels, issueService, "snowdrop", "snowdrop-team");
            return mdText;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw BotException.launderThrowable(e);
        }
    }

    private void updateGithubIssue(
            final String pMdText,
            final String pIssueTitle,
            final List<Label> pLstLabels,
            final IssueService pIssueService,
            final String pstrUser,
            final String pstrRepo)
            throws IOException {
        org.eclipse.egit.github.core.Issue issue;
        List<org.eclipse.egit.github.core.Issue> lstIssue = pIssueService.getIssues(pstrUser, pstrRepo,
                ImmutableMap.of(IssueService.FILTER_LABELS, "report", IssueService.FIELD_FILTER, "in:title:" + pIssueTitle));
        if (lstIssue.size() > 0) {
            issue = lstIssue.get(0);
            LOGGER.info("issue: " + issue);
            LOGGER.info("title: " + issue.getTitle());
            LOGGER.info("getUrl: " + issue.getUrl());
            LOGGER.info("getNumber: " + issue.getNumber());
            LOGGER.info("getMilestone: " + issue.getMilestone());
            issue.setLabels(pLstLabels);
            issue.setBody(pMdText);
            pIssueService.editIssue(pstrUser, pstrRepo, issue);
        } else {
            issue = new org.eclipse.egit.github.core.Issue();
            issue.setTitle(pIssueTitle);
            issue.setLabels(pLstLabels);
            issue.setBody(pMdText);
            pIssueService.createIssue("snowdrop", "snowdrop-team", issue);
        }
    }

}
