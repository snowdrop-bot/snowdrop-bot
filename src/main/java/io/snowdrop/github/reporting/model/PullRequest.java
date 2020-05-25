package io.snowdrop.github.reporting.model;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class PullRequest extends PanacheEntityBase implements WithDates {

  private static final String ISSUE_URL_PATTERN =  ".*/\\d+$";
  private static final Pattern ISSUE_REF =  Pattern.compile("#\\d+");
    
  @Id
  String url;
  String repository;
  int number;
  @ElementCollection
  Set<String> issueUrls;
  String title;
  String creator;
  String assignee;
  boolean open;
  Date createdAt;
  Date updatedAt;
  Date closedAt;

  public PullRequest() {

  }

  public PullRequest(String url, String repository, int number, Set<String> issueUrls, String title, String creator, String assignee,
      boolean open, Date createdAt, Date updatedAt, Date closedAt) {
    this.url = url;
    this.repository = repository;
    this.number = number;
    this.issueUrls = issueUrls;
    this.title = title;
    this.creator = creator;
    this.assignee = assignee;
    this.open = open;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.closedAt = closedAt;
  }

  public static PullRequest create(String repository, org.eclipse.egit.github.core.PullRequest pr) {
    int number = numberFromUrl(pr.getHtmlUrl());
    Set<Integer> issues = new HashSet<>();
    if (pr.getIssueUrl() != null && pr.getIssueUrl().matches(ISSUE_URL_PATTERN)) {
      int issue = numberFromUrl(pr.getIssueUrl());
      if (issue != number) { // Let's not add the internal issue url.
        issues.add(issue);
      }
    }

    if (pr.getBody() != null && !pr.getBody().isEmpty()) {
      Matcher matcher = ISSUE_REF.matcher(pr.getBody());
      while (matcher.find()) {
        issues.add(Integer.parseInt(matcher.group().substring(1)));
      }
    }

    Set<String> issueUrls = issues.stream().map(i -> pr.getIssueUrl().replace(String.valueOf(number), String.valueOf(i))).collect(Collectors.toSet());
    return new PullRequest(pr.getHtmlUrl(), repository, pr.getNumber(), issueUrls, pr.getTitle(), pr.getUser().getLogin(), null,
        pr.getState().equals("open"), pr.getCreatedAt(), pr.getUpdatedAt(), pr.getClosedAt());
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

  public Set<String> getIssueUrls() {
    return this.issueUrls;
  }

  public void setIssueUrls(Set<String> issues) {
    this.issueUrls=issueUrls;
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

  public Date getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Date updatedAt) {
    this.updatedAt = updatedAt;
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

  public Set<Integer> getIssues() {
    return this.issueUrls.stream().map(PullRequest::numberFromUrl).collect(Collectors.toSet());
  }

  public static boolean isValidIssueUrl(String issueUrl) {
    return issueUrl != null && issueUrl.matches(ISSUE_URL_PATTERN);
  } 

  public static int numberFromUrl(String url) {
    String str = url.substring(url.lastIndexOf("/") + 1);
    return Integer.parseInt(str);
  }
  /**
   * <p>Issues modified between a date range for a specific repository.</p>
   *
   * @param prepository
   * @param pdateFrom
   * @param pdateTo
   * @return
   */
  public static List<PullRequest> findByRepoAssigneeAndModifiedDate(
          final String prepository,
          final String pasignee,
          final Date pdateFrom,
          final Date pdateTo) {
      return PullRequest
              .find("repository = ?1 AND creator = ?2 AND updatedAt >= ?3 AND updatedAt <= ?4",
                      prepository, pasignee, pdateFrom, pdateTo)
              .list();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    PullRequest other = (PullRequest) obj;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

}
