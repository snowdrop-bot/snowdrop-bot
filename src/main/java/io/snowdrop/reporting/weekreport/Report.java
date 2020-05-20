package io.snowdrop.reporting.weekreport;

import java.util.Date;
import java.util.Set;

/**
 * <p>Generates the Development report.</p>
 */
abstract class Report {
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

  /**
   * <p>Generates the markdown for the report.</p>
   *
   * @return <p>Markdown text.</p>
   */
  public abstract String buildWeeklyReport();
}
