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
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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

    @ConfigProperty(name = "github.reporting.target.organization")
    String repoOrganization;

    @ConfigProperty(name = "github.reporting.target.repository")
    String repoName;

    @Inject
    GitHubClient client;

    @Inject
    GithubReportingService service;

    private void populate(Date startTime, Date endTime) {
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
    @Transactional
    public String createReport(@QueryParam("startTime") String startTimeString, @QueryParam("endTime") String endTimeString) {
        String mdText;
        String weekNumber;
        IssueService issueService = new IssueService(client);
        try {
            Date startTime = startTimeString != null ? DF.parse(startTimeString)
                    : Date.from(service.getPullRequestCollector().getStartTime().toInstant());
            Date endTime = endTimeString != null ? DF.parse(endTimeString)
                    : Date.from(service.getPullRequestCollector().getEndTime().toInstant());
            weekNumber = WEEK_YEAR_FORMAT.format(endTime);
            LOGGER.debug("week number: " + weekNumber);
            populate(startTime, endTime);
            mdText = WeeklyDevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info("weekly_development: " + mdText);
            updateGithubIssue(mdText, "weekly_development_" + weekNumber, "report", issueService, repoOrganization, repoName);
            mdText = DevelopmentReportImpl.build(startTime, endTime, users).buildWeeklyReport(Repository.findAll().list());
            LOGGER.info("dev: " + mdText);
            updateGithubIssue(mdText, "dev_" + weekNumber, "report", issueService, repoOrganization, repoName);
            return mdText;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw BotException.launderThrowable(e);
        }
    }

    /**
     * <p>Create/Update GitHub issue with the resulting information from the report.</p>
     *
     * @param pMdText
     * @param pIssueTitle
     * @param pstrLabel
     * @param pIssueService
     * @param pstrUser
     * @param pstrRepo
     * @throws IOException
     */
    private void updateGithubIssue(
            final String pMdText,
            final String pIssueTitle,
            final String pstrLabel,
            final IssueService pIssueService,
            final String pstrUser,
            final String pstrRepo)
            throws IOException {
        final List<Label> lstLabels = new LinkedList() {{
            add(new Label().setName(pstrLabel));
        }};
        org.eclipse.egit.github.core.Issue gitIssue;
        Issue issue = Issue.findByLabelTitle(pstrLabel, pIssueTitle).firstResult();
        if (issue != null) {
            gitIssue = pIssueService.getIssue(pstrUser, pstrRepo, issue.getNumber());
            gitIssue.setLabels(lstLabels);
            gitIssue.setBody(pMdText);
            gitIssue.setTitle(pIssueTitle);
            pIssueService.editIssue(pstrUser, pstrRepo, gitIssue);
        } else {
            gitIssue = new org.eclipse.egit.github.core.Issue();
            gitIssue.setTitle(pIssueTitle);
            gitIssue.setLabels(lstLabels);
            gitIssue.setBody(pMdText);
            gitIssue = pIssueService.createIssue(pstrUser, pstrRepo, gitIssue);
            issue = Issue.create(pstrUser + "/" + pstrRepo, gitIssue);
            issue.persist();
        }
    }

}
