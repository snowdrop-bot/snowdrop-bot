package io.snowdrop.security.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "bot_user")
public class BotUser extends PanacheEntityBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(BotUser.class);

  @GeneratedValue
  Long id;

  @Id
  String username;

  String password;

  String role;

  public BotUser() {
  }

  public BotUser(final String username, final String password, final String role) {
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public static BotUser create(final String username, final String password, final String role) {
    return new BotUser(username, password, role);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BotUser other = (BotUser) obj;
    if (username == null) {
      if (other.username != null)
        return false;
    } else if (!username.equals(other.username))
      return false;
    return true;
  }

  @Override public String toString() {
    StringBuffer sb = new StringBuffer("{username: ").append(username).append(", role: ").append(role).append("}");
    return sb.toString();
  }
}
