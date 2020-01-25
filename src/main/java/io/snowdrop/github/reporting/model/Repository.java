package io.snowdrop.github.reporting.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@IdClass(ForkId.class)
public class Repository extends PanacheEntityBase {

  @Id
  private String url;
  @Id
  private String parent;

  private String owner;
  private String name;
  private boolean fork;

  public Repository() {
  }

  public Repository(String url, String parent, String owner, String name, boolean fork) {
    this.url = url;
    this.owner = owner;
    this.name = name;
    this.fork = fork;
    this.parent = parent;
  }

  public static Repository fromFork(String owner, String user, String repo) {
    return new Repository("https://github.com" + owner + "/" + repo, user + "/" + repo, owner, repo, true);
  }

  public static Repository create(org.eclipse.egit.github.core.Repository repo) {
    return new Repository(repo.getUrl(),
                          repo.getParent() != null ? repo.getParent().getOwner().getLogin() + "/" + repo.getParent().getName() : null,
                          repo.getOwner().getLogin(), repo.getName(),
                          repo.isFork());
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
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
    Repository other = (Repository) obj;
    if (parent == null) {
      if (other.parent != null)
        return false;
    } else if (!parent.equals(other.parent))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

}
