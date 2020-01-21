package io.snowdrop.github.reporting.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class Issue extends PanacheEntityBase {

  @Id
  String url;
  String repository;
  int number;
  String title;
  String creator;
  String assignee;
  boolean open;
  Date createdAt;
  Date closedAt;

  public Issue() {
  }

  public Issue(String url, String repository, int number, String title, String creator, String assignee,
      boolean open, Date createdAt, Date closedAt) {
    this.url = url;
    this.repository = repository;
    this.number = number;
    this.title = title;
    this.creator = creator;
    this.assignee = assignee;
    this.open = open;
    this.createdAt = createdAt;
    this.closedAt = closedAt;
  }

  public static Issue create(String repository, org.eclipse.egit.github.core.Issue issue) {
    return new Issue(issue.getUrl(), repository, issue.getNumber(), issue.getTitle(), issue.getUser().getLogin(),
        issue.getAssignee() != null ? issue.getAssignee().getLogin() : null, issue.getState().equals("open"),
        issue.getCreatedAt(), issue.getClosedAt());

  }

  public boolean isActiveDuring(Date start, Date end) {
    if (createdAt.after(end)) {
      return false;
    }
    if (closedAt == null) {
      return true;
    }

    if (closedAt.before(start)) {
      return false;
    }

    return true;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Date getClosedAt() {
    return closedAt;
  }

  public void setClosedAt(Date closedAt) {
    this.closedAt = closedAt;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

}
