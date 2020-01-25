package io.snowdrop.github.reporting.model;

import java.util.Date;

public interface WithDates {

  Date getCreatedAt();
  Date getUpdatedAt();
  Date getClosedAt();

  default boolean isActiveDuring(Date start, Date end) {
    if (start.before(getCreatedAt()) && end.after(getCreatedAt())) {
     return true;
    }
    if (getUpdatedAt() != null && start.before(getUpdatedAt()) && end.after(getUpdatedAt())
        && (getClosedAt() == null || getClosedAt().after(start))) { //Let's just ignore issues old resolved issues recently updated.
      return true;
    }
    if (getClosedAt() != null && start.before(getClosedAt()) && end.after(getClosedAt())) {
      return true;
    }

    return false;
  }
}
