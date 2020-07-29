package io.snowdrop.reporting.weekreport;

import io.quarkus.scheduler.Scheduled;
import io.snowdrop.BotException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * Generates the weekly report.
 * </p>
 */
@ApplicationScoped
public class WeekReportService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeekReportService.class);

  @Inject
  WeekReportEndpoint weekReportEp;

  @ConfigProperty(name = "github.reporting.enabled", defaultValue = "false")
  private boolean enabled;

  @Scheduled(cron = "{report.cron.expr}")
  @Transactional
  public void weeklyExecution() {
    LOGGER.warn("Weekly Execution starting...");
    if (enabled) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date(System.currentTimeMillis()));
      cal.set(Calendar.HOUR_OF_DAY, 12);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      final Date endTime = cal.getTime();
      cal.add(Calendar.DAY_OF_MONTH, -7);
      final Date startTime = cal.getTime();
      try {
        weekReportEp.executeReport(startTime, endTime, false);
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
        throw BotException.launderThrowable(e);
      }
    }
    LOGGER.warn("...Weekly Execution starting.");
  }

}
