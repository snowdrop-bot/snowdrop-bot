package io.snowdrop.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
public class TagsEndpoint {

  @ConfigProperty(name = "github.users")
  Set<String> users;

  @GET
  @Path("/cloud")
  public List<Map<String, String>> getCloud() {
    return users.stream().map(TagsEndpoint::userMap).collect(Collectors.toList());
  }

  private static Map<String, String> userMap(String user) {
   Map<String, String> map = new HashMap<String, String>();
   map.put("label", user);
   map.put("url", "https://github.com/"+user);
   return map;
  }
}
