package io.snowdrop.github.issues.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.snowdrop.github.reporting.model.WithDates;

public class WithDatesTest {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
  private static Date START;
  private static Date END;

  @BeforeAll
  public static void setupClass() throws Exception {
    START = DATE_FORMAT.parse("01/01/2020");
    END = DATE_FORMAT.parse("08/01/2020");
  }

  @Test
  public void shouldIgnoreIssuesCreatedAfterEnd() throws Exception {
    WithDates wd = createWithDates("10/01/2020", "11/01/2020", "11/01/2020");
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2020", "11/01/2020", null);
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2020", null, null);
    assertFalse(wd.isActiveDuring(START, END));
  }

  @Test
  public void shouldIgnoreIssuesClosedBefore() throws Exception {
    WithDates wd = createWithDates("10/01/2019", "11/01/2019", "11/01/2019");
    assertFalse(wd.isActiveDuring(START, END));

    //Updated after closed
    wd = createWithDates("10/01/2019", "11/01/2019", "01/01/2020");
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2019", null, "11/01/2019");
    assertFalse(wd.isActiveDuring(START, END));
  }

  @Test
  public void shouldIgnoreStale() throws Exception {
    WithDates wd = createWithDates("10/01/2019", null, null);
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2019", "11/07/2019", null);
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2019", "11/07/2019", "10/01/2019");
    assertFalse(wd.isActiveDuring(START, END));

    wd = createWithDates("10/01/2019", "09/01/2020", "09/01/2020");
    assertFalse(wd.isActiveDuring(START, END));
  }

  @Test
  public void shouldNotIgnoreIssuesCreatedDuring() throws Exception {
    WithDates wd = createWithDates("02/01/2020", null, null);
    assertTrue(wd.isActiveDuring(START, END));

    wd = createWithDates("02/01/2020", "09/01/2020", null);
    assertTrue(wd.isActiveDuring(START, END));

    //All during
    wd = createWithDates("02/01/2020", "06/01/2020", "07/01/2020");
    assertTrue(wd.isActiveDuring(START, END));

    //Updated & Closed after
    wd = createWithDates("02/01/2020", "09/01/2020", "09/01/2020");
    assertTrue(wd.isActiveDuring(START, END));
  }

  @Test
  public void shouldNotIgnoreIssuesUpdatedDuring() throws Exception {
    WithDates wd = createWithDates("31/12/2019", "02/01/2020", null);
    assertTrue(wd.isActiveDuring(START, END));

    wd = createWithDates("31/12/2019", "02/01/2020", "10/01/2020");
    assertTrue(wd.isActiveDuring(START, END));
  }

  @Test
  public void shouldNotIgnoreIssuesClosedDuring() throws Exception {
    WithDates wd = createWithDates("31/12/2019", null, "02/01/2020");
    assertTrue(wd.isActiveDuring(START, END));

    wd = createWithDates("31/12/2019", "02/01/2020", "06/01/2020");
    assertTrue(wd.isActiveDuring(START, END));
  }



  private static final WithDates createWithDates(String createdAt, String updatedAt, String closedAt) throws Exception {
    return createWithDates(DATE_FORMAT.parse(createdAt),
                           updatedAt != null ? DATE_FORMAT.parse(updatedAt) : null,
                           closedAt != null ? DATE_FORMAT.parse(closedAt) : null);
  }

  private static final WithDates createWithDates(Date createdAt, Date updatedAt, Date closedAt) {
    return new WithDates(){
      @Override
      public Date getUpdatedAt() {
        return updatedAt;
      }

      @Override
      public Date getCreatedAt() {
        return createdAt;
      }

      @Override
      public Date getClosedAt() {
        return closedAt;
      }
    };
  }
}
