package io.snowdrop.github.issues;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.egit.github.core.Issue;

import io.snowdrop.github.reporting.GithubReportingService;

@Path("/bridge")
@Produces(MediaType.APPLICATION_JSON)
public class GithubIssueBridgeEndpoint {

    private static final String REPO = "repo";
    private static final String OPEN_ISSUES = "open";
    private static final String CLOSED_ISSUES = "closed";

    @Inject
    GithubIssueBridgeService service;

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
    public Boolean status() {
        return service.status();
    }

    @GET
    @Path("/source-repos")
    public List<String> sourceRepos() {
       return service.getBridges().stream().map(b->b.getSourceRepository()).collect(Collectors.toList());
    }

    @GET
    @Path("/now")
    public void bridgeNow() {
        service.bridgeNow();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public List<Issue> bridge() {
        return Collections.emptyList();
    }
}
