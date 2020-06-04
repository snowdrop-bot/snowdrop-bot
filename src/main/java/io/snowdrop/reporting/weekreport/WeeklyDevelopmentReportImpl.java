package io.snowdrop.reporting.weekreport;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.reporting.MarkdownHelper;
import io.snowdrop.reporting.ReportConstants;
import io.snowdrop.reporting.model.Associate;
import io.snowdrop.reporting.model.Issue;
import io.snowdrop.reporting.model.IssueSource;
import net.steppschuh.markdowngenerator.MarkdownSerializationException;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.TextBuilder;

/**
 * <p>Generates the Weekly Development report.</p>
 * <p>This report consists of all the Issue existing in the weekly development repository that have the following characteristics:
 * <ul>
 *     <li>Have been modified in the specified date range or are still open</li>
 *     <li>Are not labeled report or have no label at all</li>
 * </ul>
 * </p>
 * <p>The SQL sentence used is the following: <pre>repository = ?1 AND ((updatedAt >= ?2 AND updatedAt <= ?3) OR open = true) AND (label != 'report' or label is null)</pre></p>
 * <p>For the presentation, the issues are grouped by assignee and label.</p>
 */
public class WeeklyDevelopmentReportImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyDevelopmentReportImpl.class);

  /**
   * <p>Users to which the report will be applied</p>
   */
  protected Set<String> users;

  /**
   * <p>Identifies the begining of the time period for the report</p>
   */
  protected Date startDate = null;

  /**
   * <p>Identifies the begining of the time period for the report</p>
   */
  protected Date endDate = null;

  String mdClosedFormat = null;

  String mdOpenFormat = null;

  String mdOldFormat = null;

  String mdAncientFormat = null;

  public WeeklyDevelopmentReportImpl(
  final Date startDate,
  final Date endDate,
  final Set<String> users,
  final String mdOpenFormat,
  final String mdOldFormat,
  final String mdAncientFormat,
  final String mdClosedFormat) {
    this.users = users;
    this.startDate = startDate;
    this.endDate = endDate;
    this.mdAncientFormat = mdAncientFormat;
    this.mdClosedFormat = mdClosedFormat;
    this.mdOldFormat = mdOldFormat;
    this.mdOpenFormat = mdOpenFormat;
  }

  public static WeeklyDevelopmentReportImpl build(
  final Date startDate, final Date endDate, final Set<String> users,
  final String mdOpenFormat,
  final String mdOldFormat,
  final String mdAncientFormat,
  final String mdClosedFormat) {
    return new WeeklyDevelopmentReportImpl(startDate, endDate, users, mdOpenFormat, mdOldFormat, mdAncientFormat, mdClosedFormat);
  }

  /**
   * <p>Generates the markdown for the report.</p>
   *
   * @return
   */
  public String buildWeeklyReport(String reportName) {
    StringBuilder sb = new StringBuilder(MarkdownHelper.addHeadingTitle(reportName, 1)).append(ReportConstants.CR);
    ZonedDateTime now = ZonedDateTime.now();
    ZonedDateTime twoWeeksAgo = now.minusWeeks(2);
    ZonedDateTime oneMonthAgo = now.minusMonths(1);
    String repoName = ReportConstants.WEEK_DEV_REPO_OWNER + "/" + ReportConstants.WEEK_DEV_REPO_NAME;
    Issue.findByIssuesForWeeklyDevelopmentReport(repoName, startDate, endDate).list().stream()
    .collect(Collectors.groupingBy(Issue::getAssignee, TreeMap::new, Collectors.toSet()))
    .entrySet().forEach(eachAssignee -> {
      String assignee = eachAssignee.getKey();
      String associateName = Associate.getAssociateName(assignee, IssueSource.GITHUB);
      sb.append(ReportConstants.CR).append(ReportConstants.CR).append(MarkdownHelper.addHeadingTitle(associateName, 2)).append(ReportConstants.CR);
      UnorderedList labelUnorderedList = new UnorderedList();
      eachAssignee.getValue().stream().collect(Collectors.groupingBy(Issue::getLabel, TreeMap::new, Collectors.toSet())).entrySet().forEach(eachLabel -> {
        String label = eachLabel.getKey();
        labelUnorderedList.getItems().add(label);
        UnorderedList issueUnorderedList = new UnorderedList();
        eachLabel.getValue().stream().collect(Collectors.groupingBy(Issue::getStatus, TreeMap::new, Collectors.toSet())).entrySet().forEach(eachStatus -> {
          eachStatus.getValue().stream().forEach(eachIssue -> {
            TextBuilder issueTextB = new TextBuilder();
            Date dateCreatedAt = eachIssue.getCreatedAt();
            String strMdColor = mdClosedFormat;
            if (eachIssue.isOpen()) {
              strMdColor = mdOpenFormat;
              if (dateCreatedAt.toInstant().isBefore(oneMonthAgo.toInstant())) {
                strMdColor = mdAncientFormat;
              } else if (dateCreatedAt.toInstant().isBefore(twoWeeksAgo.toInstant())) {
                strMdColor = mdOldFormat;
              }
            }
            issueTextB.append(new Text(" ")).append(strMdColor).append(" [`").append(eachIssue.getStatus()).append("`] ").append(eachIssue.getTitle())
            .append(" - ")
            .append(new Link(eachIssue.getUrl()));
            issueUnorderedList.getItems().add(issueTextB);
          });
        });
        labelUnorderedList.getItems().add(issueUnorderedList);
      });
      try {
        sb.append(labelUnorderedList.serialize());
      } catch (MarkdownSerializationException pE) {
        pE.printStackTrace();
      }
    });
    sb.append(ReportConstants.CR).append(ReportConstants.CR).append(MarkdownHelper.addHeadingTitle("Legend", 2)).append(ReportConstants.CR);
    UnorderedList legendUnorderedList = new UnorderedList();
    TextBuilder legendTextB = new TextBuilder();
    legendTextB.append(new Text(" ")).append(mdOpenFormat).append(" : Open but age <= 2 weeks");
    legendUnorderedList.getItems().add(legendTextB);
    legendTextB = new TextBuilder();
    legendTextB.append(new Text(" ")).append(mdOldFormat).append(" : Open but age is >= 2 weeks & <  1month");
    legendUnorderedList.getItems().add(legendTextB);
    legendTextB = new TextBuilder();
    legendTextB.append(new Text(" ")).append(mdAncientFormat).append(" : Open but age > 1 month");
    legendUnorderedList.getItems().add(legendTextB);
    legendTextB = new TextBuilder();
    legendTextB.append(new Text(" ")).append(mdClosedFormat).append(" : Close");
    legendUnorderedList.getItems().add(legendTextB);
    try {
      sb.append(legendUnorderedList.serialize());
    } catch (MarkdownSerializationException pE) {
      pE.printStackTrace();
    }
    return sb.toString();
  }

}
