package io.snowdrop.reporting.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.snowdrop.github.reporting.model.WithDates;

@Entity
public class Issue extends PanacheEntityBase implements WithDates {

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

    public Issue() {
    }

    public Issue(
            final String pUrl,
            final String pRepository,
            final int pNumber,
            final String pTitle,
            final String pCreator,
            final String pAssignee,
            final boolean pOpen,
            final Date pCreatedAt,
            final Date pUpdatedAt,
            final Date pClosedAt,
            final String pSource,
            final String pStatus,
            final String pLabel,
            final String pIssueGroup,
            final String pAffectedVersion,
            final String pFixVersion) {
        url = pUrl;
        repository = pRepository;
        number = pNumber;
        title = pTitle;
        creator = pCreator;
        assignee = pAssignee;
        open = pOpen;
        createdAt = pCreatedAt;
        updatedAt = pUpdatedAt;
        closedAt = pClosedAt;
        source = pSource;
        status = pStatus;
        label = pLabel;
        issueGroup = pIssueGroup;
        affectedVersion = pAffectedVersion;
        fixVersion = pFixVersion;
    }

    public static Issue create(String repository, org.eclipse.egit.github.core.Issue issue) {
        return new Issue(issue.getHtmlUrl(), repository, issue.getNumber(), issue.getTitle(), issue.getUser().getLogin(),
                issue.getAssignee() != null ? issue.getAssignee().getLogin() : null, IssueOpen.isOpen(issue.getState()),
                issue.getCreatedAt(), issue.getUpdatedAt(), issue.getClosedAt(), IssueSource.GITHUB.name(), issue.getState(),
                ((issue.getLabels() != null && issue.getLabels().size() > 0) ? issue.getLabels().get(0).getName() : null), null, null, null);
    }

    /**
     * <p>Issues modified between a date range.</p>
     *
     * @param pdateFrom
     * @param pdateTo
     * @return
     */
    public static List<Issue> findByModifiedDate(final Date pdateFrom, final Date pdateTo) {
        return Issue.find("SELECT issues FROM Issue issues WHERE updatedAt >= ?1 AND updatedAt <= ?2 ORDER BY assignee, repository, label", pdateFrom, pdateTo)
                .list();
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
        return Issue.find("repository = ?1 AND (updatedAt >= ?2 AND updatedAt <= ?3 OR open = true) AND (label != 'report' or label is null)", Sort.ascending("assignee", "label", "updatedAt"), prepository, pdateFrom, pdateTo);
    }

    /**
     * <p>Issues modified between a date range for a specific repository.</p>
     *
     * @param prepository
     * @param pdateFrom
     * @param pdateTo
     * @return
     */
    public static List<Issue> findByRepoAssigneeAndModifiedDate(final String prepository, final String pasignee, final Date pdateFrom, final Date pdateTo) {
        return Issue
                .find("repository = ?1 AND assignee = ?2 AND updatedAt >= ?3 AND updatedAt <= ?4 ", Sort.ascending("updatedAt"), prepository, pasignee, pdateFrom, pdateTo)
                .list();
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
