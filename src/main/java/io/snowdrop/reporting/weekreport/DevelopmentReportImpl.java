package io.snowdrop.reporting.weekreport;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p>Generates the weekly report.</p>
 */
public class DevelopmentReportImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentReportImpl.class);

    private Set<String> users;
    private Date startDate = null;
    private Date endDate = null;

    public DevelopmentReportImpl(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
        users = pusers;
        startDate = pStartDate;
        endDate = pEndDate;
    }

    public static DevelopmentReportImpl build(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
        return new DevelopmentReportImpl(pStartDate, pEndDate,pusers);
    }

    /**
     * @param pByModifiedDate
     * @return
     */
    public String buildWeeklyReport(final List<Repository> pByModifiedDate) {
        StringBuilder sb = new StringBuilder();
        users.stream().forEach(eachAssignee -> {
            sb.append(ReportConstants.CR).append(ReportConstants.CR).append(MarkdownHelper.addHeadingTitle(eachAssignee, 2)).append(ReportConstants.CR);
            UnorderedList repoUnorderedList = new UnorderedList();
            Repository.findByExcOwnerName(ReportConstants.WEEK_DEV_REPO_OWNER,ReportConstants.WEEK_DEV_REPO_NAME).list().stream().forEach(eachRepo -> {
                String repoName = ((Repository) eachRepo).getOwner() + "/" + ((Repository) eachRepo).getName();
//                LOGGER.info("eachAssignee/repoName: " + eachAssignee+"/"+repoName);
                UnorderedList issueUnorderedList = new UnorderedList();
                List<Issue> lstIssue = Issue
                        .findByRepoAssigneeAndModifiedDate(repoName, eachAssignee, startDate, endDate);
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
