package io.snowdrop.reporting.weekreport;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class WeeklyDevelopmentReportImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyDevelopmentReportImpl.class);

    private Set<String> users;
    private Date startDate = null;
    private Date endDate = null;

    public WeeklyDevelopmentReportImpl(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
        users = pusers;
        startDate = pStartDate;
        endDate = pEndDate;
    }

    public static WeeklyDevelopmentReportImpl build(final Date pStartDate, final Date pEndDate, final Set<String> pusers) {
        return new WeeklyDevelopmentReportImpl(pStartDate, pEndDate, pusers);
    }

    /**
     * @param pByModifiedDate
     * @return
     */
    public String buildWeeklyReport(final List<Repository> pByModifiedDate) {
        StringBuilder sb = new StringBuilder();
        String repoName = ReportConstants.WEEK_DEV_REPO_OWNER + "/" + ReportConstants.WEEK_DEV_REPO_NAME;
        Issue.findByIssuesForWeeklyDevelopmentReport(repoName, startDate, endDate).list().stream()
                .collect(Collectors.groupingBy(Issue::getAssignee, Collectors.toSet()))
                .entrySet().forEach(eachAssignee -> {
            String assignee = eachAssignee.getKey();
            sb.append(ReportConstants.CR).append(ReportConstants.CR).append(MarkdownHelper.addHeadingTitle(assignee, 2)).append(ReportConstants.CR);
            UnorderedList labelUnorderedList = new UnorderedList();
            eachAssignee.getValue().stream().collect(Collectors.groupingBy(Issue::getLabel, Collectors.toSet())).entrySet().forEach(eachLabel -> {
                String label = eachLabel.getKey();
                labelUnorderedList.getItems().add(label);
                UnorderedList issueUnorderedList = new UnorderedList();
                eachLabel.getValue().stream().forEach(eachIssue -> {
                    TextBuilder issueTextB = new TextBuilder();
                    issueTextB.append(new Text("<span style=\"color:" + (eachIssue.isOpen() ? "green" : "orange") + "\">[")).append(eachIssue.getStatus())
                            .append("]</span> ").append(eachIssue.getTitle()).append(" - ").append(new Link(eachIssue.getUrl()));
                    issueUnorderedList.getItems().add(issueTextB);
                });
                labelUnorderedList.getItems().add(issueUnorderedList);
            });
            try {
                sb.append(labelUnorderedList.serialize());
            } catch (MarkdownSerializationException pE) {
                pE.printStackTrace();
            }
        });
        return sb.toString();
    }

}
