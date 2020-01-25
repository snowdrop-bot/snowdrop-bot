/**
 * Copyright 2018 The original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
**/

package io.snowdrop.github.reporting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Service;

import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

@Path("/reporting")
public class ReportingEndpoint {

    private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

    @Inject
    GithubReportingService service;

    @GET
    @Path("/enable")
    public void enable() {
        service.enable();
    }

    @GET
    @Path("/disable")
    public void disable() {
        service.disable();
    }

    @GET
    @Path("/status")
    public boolean status() {
        return service.status();
    }

    @GET
    @Path("/collect/issues")
    public void collectIssues() {
        service.collectIssues();
    }

    @GET
    @Path("/collect/pull-requests")
    public void collectPullRequests() {
        service.collectPullRequests();
    }

    @GET
    @Path("/orgs")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> organizations() {
        return service.getReporting().getOrganizations();
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> users() {
        return service.getReporting().getUsers();
    }

    @GET
    @Path("/start-time")
    @Produces(MediaType.APPLICATION_JSON)
    public Date recomendedStartDate() {
        return Date.from(service.getReporting().getStartTime().toInstant());
    }

    @GET
    @Path("/end-time")
    @Produces(MediaType.APPLICATION_JSON)
    public Date recomendedEndDate() {
        return Date.from(service.getReporting().getEndTime().toInstant());
    }

    @GET
    @Path("/repositories")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<Repository>> repositories() {
        return service.getReporting().getRepositories();
    }

    @GET
    @Path("/repositories/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Repository> repositories(@PathParam("user") String user) {
        return service.getReporting().getRepositories().get(user);
    }

    @GET
    @Path("/data/pr")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<PullRequest>> pullRequestData(@QueryParam("startTime") String startTimeString,
            @QueryParam("endTime") String endTimeString) throws ParseException {
        Map<String, Set<PullRequest>> map = new HashMap<>();
        Date startTime = startTimeString != null ? DF.parse(startTimeString)
                : Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = endTimeString != null ? DF.parse(endTimeString)
                : Date.from(service.getReporting().getEndTime().toInstant());
        Set<PullRequest> pullRequests = service.getReporting().getPullRequests().values().stream()
                .flatMap(s -> s.stream()).filter(p -> p.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());
        map.put("data", pullRequests);
        return map;
    }


    @GET
    @Path("/data/issues")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<Issue>> issueData(@QueryParam("startTime") String startTimeString,
            @QueryParam("endTime") String endTimeString) throws ParseException {
        Map<String, Set<Issue>> map = new HashMap<>();
        Date startTime = startTimeString != null ? DF.parse(startTimeString)
                : Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = endTimeString != null ? DF.parse(endTimeString)
                : Date.from(service.getReporting().getEndTime().toInstant());
        Set<Issue> issues = service.getReporting().getIssues().values().stream().flatMap(s -> s.stream())
                .filter(i -> i.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());
        map.put("data", issues);
        return map;
    }

    @GET
    @Path("/pr/repo/{user}/{repo}/user/{creator}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<PullRequest> pullRequests(@PathParam("creator") String creator, @PathParam("user") String user,
            @PathParam("repo") String repo, @QueryParam("startTime") String startTimeString,
            @QueryParam("endTime") String endTimeString) throws ParseException {
        Date startTime = startTimeString != null ? DF.parse(startTimeString)
                : Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = endTimeString != null ? DF.parse(endTimeString)
                : Date.from(service.getReporting().getEndTime().toInstant());

        return service.getReporting().userPullRequests(creator, Repository.fromFork(creator, user, repo), "all")
            .stream()
            .filter(i -> i.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());
    }

    @GET
    @Path("/issues/repo/{user}/{repo}/user/{assignee}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Issue> issues(@PathParam("assignee") String assignee, @PathParam("user") String user,
            @PathParam("repo") String repo, @QueryParam("startTime") String startTimeString,
            @QueryParam("endTime") String endTimeString) throws ParseException {
        Date startTime = startTimeString != null ? DF.parse(startTimeString)
                : Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = endTimeString != null ? DF.parse(endTimeString)
                : Date.from(service.getReporting().getEndTime().toInstant());
        return service.getReporting().userIssues(assignee, Repository.fromFork(assignee, user, repo), "all")
            .stream()
            .filter(i -> i.isActiveDuring(startTime, endTime)).collect(Collectors.toSet());
    }
}
