package io.snowdrop.security.model;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class BotUserRepository implements PanacheRepository<BotUser> {

  public BotUser findByUsername(String username) {
    return find("username", username).firstResult();
  }

  public BotUser createUser(final String username, final String password, final String role) {
    BotUser botUser = new BotUser(username, password, role);
    persist(botUser);
    return botUser;
  }
}
