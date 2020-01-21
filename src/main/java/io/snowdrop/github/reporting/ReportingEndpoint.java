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
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Service;

import io.snowdrop.github.reporting.model.Issue;
import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;

@Path("/reporting")
public class ReportingEndpoint {

    @Inject
    GithubReportingService service;

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
    @Path("/pr")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<PullRequest>> pullRequests() {
        return service.getReporting().getPullRequests();
    }

    @GET
    @Path("/data/pr")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<PullRequest>> pullRequestData() {
        Map<String, Set<PullRequest>> map = new HashMap<>();
        Date startTime = Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = Date.from(service.getReporting().getEndTime().toInstant());
        Set<PullRequest> pullRequests = service.getReporting().getPullRequests()
            .values()
            .stream()
            .flatMap(s -> s.stream())
            .filter(p -> p.isActiveDuring(startTime, endTime))
            .collect(Collectors.toSet());
        map.put("data", pullRequests);
        return map;
    }


    @GET
    @Path("/pr/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<PullRequest> pullRequests(@PathParam("user") String user) {
        return service.getReporting().getPullRequests().get(user);
    }

    @GET
    @Path("/issues")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<Issue>> issues() {
        return service.getReporting().getIssues();
    }

    @GET
    @Path("/data/issues")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<Issue>> issueData() {
        Map<String, Set<Issue>> map = new HashMap<>();
        Date startTime = Date.from(service.getReporting().getStartTime().toInstant());
        Date endTime = Date.from(service.getReporting().getEndTime().toInstant());
        Set<Issue> issues = service.getReporting().getIssues()
            .values()
            .stream()
            .flatMap(s -> s.stream())
            .filter(i -> i.isActiveDuring(startTime, endTime))
            .collect(Collectors.toSet());
        map.put("data", issues);
        return map;
    }

    @GET
    @Path("/issues/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Issue> issues(@PathParam("user") String user) {
        return service.getReporting().getIssues().get(user);
    }
}
