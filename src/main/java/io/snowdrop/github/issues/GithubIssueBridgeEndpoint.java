package io.snowdrop.github.issues;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.egit.github.core.Issue;

@Path("/bridge")
public class GithubIssueBridgeEndpoint {
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public List<Issue> bridge() {
      return Collections.emptyList();
    }
}
