package io.snowdrop.reporting.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.snowdrop.github.reporting.model.WithDates;
import org.eclipse.egit.github.core.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Entity
public class Issue extends PanacheEntityBase implements WithDates {

  private static final Logger LOGGER = LoggerFactory.getLogger(Issue.class);

  private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  public static final String PROGRESS_LABEL_PREFIX = "progress:";
  public static final String PROGRESS_LABEL_SUFFIX = "%";

  @Id
  String url;
  String repository;
  int number;
  String title;
  String creator;
  String assignee;
  boolean open;
  Date createdAt;
  Date updatedAt;
  Date closedAt;
  String source;
  String status;
  String label;
  String issueGroup;
  String affectedVersion;
  String fixVersion;
  int progress;

  /**
   * <p>Age of the issue:
   * <ul>
   * <li>0: Open with age <= 2 weeks</li>
   * <li>1: Open with age between 2 weeks & 1 month</li>
   * <li>2: Open with age > 1 month</li>
   * <li>3: Closed</li>
   * </ul>
   * </p>
   */
  @Transient
  Integer statusAge;

  public Issue() {
  }

  public Issue(
  String url,
  String repository,
  int number,
  String title,
  String creator,
  String assignee,
  boolean open,
  Date createdAt,
  Date updatedAt,
  Date closedAt,
  String source,
  String status,
  String label,
  String issueGroup,
  String affectedVersion,
  String fixVersion,
  int progress) {
    this.url = url;
    this.repository = repository;
    this.number = number;
    this.title = title;
    if (this.title.length()>255) {
      this.title = this.title.substring(0,254);
    }
    this.creator = creator;
    this.assignee = assignee;
    this.open = open;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.closedAt = closedAt;
    this.source = source;
    this.status = status;
    this.label = label;
    this.issueGroup = issueGroup;
    this.affectedVersion = affectedVersion;
    this.fixVersion = fixVersion;
    this.progress = progress;
    updateStatusAge();
  }

  public static Issue create(String repository, org.eclipse.egit.github.core.Issue issue) {
    int progress = extractProgress(issue);
    return new Issue(issue.getHtmlUrl(), repository, issue.getNumber(), issue.getTitle(), issue.getUser().getLogin(),
    issue.getAssignee() != null ? issue.getAssignee().getLogin() : null, IssueOpen.isOpen(issue.getState()),
    issue.getCreatedAt(), issue.getUpdatedAt(), issue.getClosedAt(), IssueSource.GITHUB.name(), issue.getState(),
    ((issue.getLabels() != null && issue.getLabels().size() > 0) ? issue.getLabels().get(0).getName() : null), null, null, null, progress);

  }

  private static int extractProgress(org.eclipse.egit.github.core.Issue issue) {
    int progress = 0;
    if (issue.getLabels() != null && issue.getLabels().size() > 0) {
      Optional<Label> optProgressLabel = issue.getLabels().stream().filter(l -> l.getName().contains("progress")).findFirst();
      if (optProgressLabel.isPresent()) {
        Label progressLabel = optProgressLabel.get();
        int start = progressLabel.getName().lastIndexOf(PROGRESS_LABEL_PREFIX);
        int end = progressLabel.getName().lastIndexOf(PROGRESS_LABEL_SUFFIX);
        if (end < start) {
          throw new IllegalStateException("Issue: " + issue.getNumber() + ". Reference is malformed. No suffix found.");
        }
        progress = Integer.parseInt(progressLabel.getName().substring(start + PROGRESS_LABEL_PREFIX.length(), end));

      }
    }
    return progress;
  }

  public static Issue create(String repository, com.atlassian.jira.rest.client.api.domain.Issue issue) {
    LOGGER.info(Issue.class.getName() + "#create(String,com.atlassian.jira.rest.client.api.domain.Issue)...");
    LOGGER.info(Issue.class.getName() + "  -> repository: " + repository);
    LOGGER.info(Issue.class.getName() + "  -> resolutiondate: " + issue.getField("resolutiondate"));
    LOGGER.info(Issue.class.getName() + "  -> issue: " + issue);
    Date resolutionDate = null;
    if (issue.getField("resolutiondate") != null && issue.getField("resolutiondate").getValue() != null) {
      try {
        resolutionDate = DF.parse((String) issue.getField("resolutiondate").getValue());
      } catch (ParseException e) {
        LOGGER.warn(e.getMessage(), e);
      }
    }
    return new Issue("https://issues.redhat.com/browse/" + issue.getKey(), repository, Integer.valueOf(issue.getKey().split("-")[1]), issue.getSummary()
    , issue.getReporter().getAccountId(), issue.getAssignee() != null ? issue.getAssignee().getName() : null, issue.getResolution() == null,
    issue.getCreationDate().toDate(), issue.getUpdateDate().toDate(), resolutionDate, IssueSource.JIRA.name(), issue.getStatus().getName(),
    ((issue.getLabels() != null && issue.getLabels().size() > 0) ? issue.getLabels().iterator().next() : null), null,
    issue.getAffectedVersions() != null && issue.getAffectedVersions().iterator().hasNext() ?
    issue.getAffectedVersions().iterator().next().getName() :
    null,
    issue.getFixVersions() != null && issue.getFixVersions().iterator().hasNext() ? issue.getFixVersions().iterator().next().getName() : null,0);
  }

  /**
   * <p>Issue for weekly report.</p>
   *
   * @param pstrLabel
   * @param pstrTitle
   * @return
   */
  public static PanacheQuery<Issue> findByLabelTitle(final String pstrLabel, final String pstrTitle) {
    return Issue.find("label = ?1 AND title like ?2 ", Sort.ascending("number"), pstrLabel, pstrTitle);
  }

  /**
   * <p>Issues modified between a date range for a specific repository.</p>
   *
   * @param prepository
   * @param pdateFrom
   * @param pdateTo
   * @return
   */
  public static PanacheQuery<Issue> findByIssuesForWeeklyDevelopmentReport(final String prepository, final Date pdateFrom, final Date pdateTo) {
    return Issue.find(
    "( (repository != ?1 AND ((updatedAt >= ?2 AND updatedAt <= ?3) OR (createdAt >= ?2 AND createdAt <= ?3) ) ) OR (repository = ?1 AND ( (updatedAt >= ?2 AND updatedAt <= ?3) OR (createdAt >= ?2 AND createdAt <= ?3) OR open = true))) AND (label != 'report' or label is null) AND assignee is not null and source = ?4",
    Sort.ascending("assignee", "label", "updatedAt"), prepository, pdateFrom, pdateTo, IssueSource.GITHUB.name());
  }

  /**
   * <p>Issues modified between a date range for a specific repository and assignee.</p>
   *
   * @param prepository
   * @param pasignee
   * @param pdateFrom
   * @param pdateTo
   * @return
   */
  public static PanacheQuery<Issue> findByRepoAssigneeAndModifiedDate(
  final String prepository,
  final String pasignee,
  final Date pdateFrom,
  final Date pdateTo) {
    return Issue
    .find("repository = ?1 AND assignee = ?2 AND updatedAt >= ?3 AND updatedAt <= ?4 ", Sort.ascending("updatedAt"), prepository, pasignee,
    pdateFrom, pdateTo);
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
    updateStatusAge();
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
    updateStatusAge();
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String pSource) {
    source = pSource;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String pStatus) {
    status = pStatus;
  }

  public String getLabel() {
    return label == null ? "" : label;
  }

  public void setLabel(final String pLabel) {
    label = pLabel;
  }

  public String getIssueGroup() {
    return issueGroup;
  }

  public void setIssueGroup(final String pIssueGroup) {
    issueGroup = pIssueGroup;
  }

  public String getAffectedVersion() {
    return affectedVersion;
  }

  public void setAffectedVersion(final String pAffectedVersion) {
    affectedVersion = pAffectedVersion;
  }

  public String getFixVersion() {
    return fixVersion;
  }

  public void setFixVersion(final String pFixVersion) {
    fixVersion = pFixVersion;
  }

  public int getProgress() {
    return progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
  }

  public Integer getStatusAge() {
    updateStatusAge();
    return statusAge;
  }

  public void setStatusAge(Integer statusAge) {
    this.statusAge = statusAge;
  }

  private void updateStatusAge() {
    if (createdAt != null) {
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime twoWeeksAgo = now.minusWeeks(2);
      ZonedDateTime oneMonthAgo = now.minusMonths(1);
      statusAge = 3;
      if (open) {
        statusAge = 0;
        if (createdAt.toInstant().isBefore(oneMonthAgo.toInstant())) {
          statusAge = 2;
        } else if (createdAt.toInstant().isBefore(twoWeeksAgo.toInstant())) {
          statusAge = 1;
        }
      }
    }
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
    Issue other = (Issue) obj;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }
}
