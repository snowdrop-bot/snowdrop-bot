package io.snowdrop.security;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.security.model.BotUser;
import io.snowdrop.security.model.BotUserRepository;

/**
 * <p>
 * API for managing users.
 * </p>
 */
@Path("/security/user")
public class UserMgmtEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserMgmtEndpoint.class);

  @Inject
  BotUserRepository repoBotUser;

  @POST
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response createUser(
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("role") String role) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("#createUser(String,String,String) - {},{}", username, role);
    }
    BotUser storedBotUser = repoBotUser.findByUsername(username);
    if (storedBotUser == null) {
      repoBotUser.createUser(username, password, role);
      return Response.accepted(storedBotUser).build();
    } else {
      return Response.notModified().build();
    }
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listUsers() {
    return Response.accepted(repoBotUser.listAll()).build();
  }

  @POST
  @Path("/delete")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response delete(@Context SecurityContext sec, @FormParam("username") String username) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("#delete(String) - {}", username);
    }
    if (sec != null && sec.getUserPrincipal() != null) {
      if (!username.equals(sec.getUserPrincipal().getName())) {
        BotUser storedBotUser = repoBotUser.findByUsername(username);
        if (storedBotUser != null) {
          repoBotUser.delete(storedBotUser);
          return Response.accepted().build();
        }
      }
    }
    return Response.notModified().build();
  }

  @PUT
  @Path("/changepw")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public Response changePassword(@Context SecurityContext sec, @FormParam("password") String password) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("#changePassword(String) {}", sec.getUserPrincipal().getName());
    }
    BotUser storedBotUser = repoBotUser.findByUsername(sec.getUserPrincipal().getName());
    if (storedBotUser != null) {
      storedBotUser.setPassword(password);
      repoBotUser.persist(storedBotUser);
      return Response.accepted().build();
    } else {
      return Response.notModified().build();
    }
  }

}
