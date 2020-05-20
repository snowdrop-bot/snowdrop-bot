package io.snowdrop.reporting.weekreport;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.snowdrop.github.reporting.model.PullRequest;
import io.snowdrop.github.reporting.model.Repository;
import io.snowdrop.reporting.MarkdownHelper;
import io.snowdrop.reporting.ReportConstants;
import io.snowdrop.reporting.model.Issue;
import net.steppschuh.markdowngenerator.MarkdownSerializationException;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.TextBuilder;

/**
 * <p>Generates the Development report.</p>
 * <p>This report consists in all the Issue and PR from all the gathered repositories, having the following characteristics:
 * <ul>
 *     <li>Are assigned assigned to any of the associates</li>
 *     <li>Where changed inside the date period informed</li>
 * </ul>
 * </p>
 * <p>The SQL sentence used is the following: <pre>repository = ?1 AND assignee = ?2 AND updatedAt >= ?3 AND updatedAt <= ?4</pre></p>
 * <p>For the presentation, the issues are grouped by assignee and label.</p>
 */
public class DevelopmentReportImpl extends Report {

  public DevelopmentReportImpl(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
    users = pusers;
    startDate = pStartDate;
    endDate = pEndDate;
  }

  public static DevelopmentReportImpl build(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
    return new DevelopmentReportImpl(pStartDate, pEndDate, pusers);
  }

  public String buildWeeklyReport() {
    StringBuilder sb = new StringBuilder();
    users.stream().forEach(eachAssignee -> {
      sb.append(ReportConstants.CR).append(ReportConstants.CR).append(MarkdownHelper.addHeadingTitle(eachAssignee, 2)).append(ReportConstants.CR);
      UnorderedList repoUnorderedList = new UnorderedList();
      Repository.findByExcOwnerName(ReportConstants.WEEK_DEV_REPO_OWNER, ReportConstants.WEEK_DEV_REPO_NAME).list().stream().forEach(eachRepo -> {
        String repoName = ((Repository) eachRepo).getOwner() + "/" + ((Repository) eachRepo).getName();
        UnorderedList issueUnorderedList = new UnorderedList();
        List<Issue> lstIssue = Issue.findByRepoAssigneeAndModifiedDate(repoName, eachAssignee, startDate, endDate).list();
        if (lstIssue.size() > 0) {
          repoUnorderedList.getItems().add(repoName + " - Issues");
          lstIssue.stream().forEach(eachIssue -> {
            TextBuilder issueTextB = new TextBuilder();
            issueTextB.append(new Text(eachIssue.getTitle())).append(" - ").append(new Link(eachIssue.getUrl()));
            issueUnorderedList.getItems().add(issueTextB);
          });
          repoUnorderedList.getItems().add(issueUnorderedList);
        }
        List<PullRequest> lstPullRequest = PullRequest
            .findByRepoAssigneeAndModifiedDate(repoName, eachAssignee, startDate, endDate);
        if (lstPullRequest.size() > 0) {
          UnorderedList prUnorderedList = new UnorderedList();
          repoUnorderedList.getItems().add(repoName + " - PRs");
          lstPullRequest.stream().forEach(eachPR -> {
            TextBuilder issueTextB = new TextBuilder();
            issueTextB.append(new Text(eachPR.getTitle())).append(" - ").append(new Link(eachPR.getUrl()));
            prUnorderedList.getItems().add(issueTextB);
          });
          repoUnorderedList.getItems().add(prUnorderedList);
        }
      });
      try {
        sb.append(repoUnorderedList.serialize());
      } catch (MarkdownSerializationException pE) {
        pE.printStackTrace();
      }
    });
    return sb.toString();
  }

}
