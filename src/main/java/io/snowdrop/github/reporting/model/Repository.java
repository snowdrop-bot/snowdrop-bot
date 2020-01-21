package io.snowdrop.github.reporting.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class Repository extends PanacheEntityBase {

  @Id
  private String url;
  private String owner;
  private String name;
  private boolean fork;
  private String parent;

  public Repository() {
  }

  public Repository(String url, String owner, String name, boolean fork, String parent) {
    this.url = url;
    this.owner = owner;
    this.name = name;
    this.fork = fork;
    this.parent = parent;
  }

  public static Repository create(org.eclipse.egit.github.core.Repository repo) {
    return new Repository(repo.getUrl(), repo.getOwner().getLogin(), repo.getName(), repo.isFork(),
        repo.getParent() != null ? repo.getParent().getOwner().getLogin() + "/" + repo.getParent().getName() : null);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isFork() {
    return fork;
  }

  public void setFork(boolean fork) {
    this.fork = fork;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

}
