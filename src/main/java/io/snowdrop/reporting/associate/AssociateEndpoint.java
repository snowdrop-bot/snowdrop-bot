package io.snowdrop.reporting.associate;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.reporting.model.Associate;
import io.snowdrop.reporting.model.IssueSource;

/**
 * <p>
 * Manages associates.
 * </p>
 * curl -X PUT http://localhost:8080/associate/create?alias=jacobdotcosta
 */
@Path("/associate")
public class AssociateEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssociateEndpoint.class);

  @POST
  @Path("/create")
  @Produces(MediaType.TEXT_PLAIN)
  @Transactional
  public Response createAssociate(
  @FormParam("associate") String associate,
  @FormParam("alias") String alias,
  @FormParam("source") String source,
  @FormParam("name") String name) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("#createAssociate(String,String,String) - {},{},{},{}", associate, alias, source, name);
    }
    Associate storedAssociate = Associate.findById(associate);
    IssueSource issueSource = IssueSource.valueOf(source);
    if (storedAssociate == null) {
      storedAssociate = Associate.create(associate, alias, issueSource.name(), name);
      storedAssociate.persist();
      return Response.accepted(storedAssociate).build();
    } else {
      return Response.notModified().build();
    }
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Associate> listAssociates() {
    return Associate.listAll();
  }

  @POST
  @Path("/delete")
  @Produces(MediaType.TEXT_PLAIN)
  public Response delete(@FormParam("associate") String associate) {
     Associate storedAssociate = Associate.findById(associate);
     if (storedAssociate != null) {
       storedAssociate.delete();
       return Response.accepted().build();
     } else {
       return Response.notModified().build();
     }
  }

}
