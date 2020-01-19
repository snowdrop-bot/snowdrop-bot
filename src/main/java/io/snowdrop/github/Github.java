
package io.snowdrop.github;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import io.snowdrop.BotException;

public class Github {

  private static final String GITHUB_DATE_FORMAT_PATTERN = "YYYY-MM-DDTHH:MM:SSZ";
  private static final String USER_REPO_SEPARATOR = "/";

  private static final String CREATOR = "creator";
  private static final String ASSIGNEE = "assignee";
  private static final String SINCE = "since";
  private static final String STATE = "state";

  private static final String STATE_OPEN = "open";
  private static final String STATE_CLOSED = "closed";


  public static String user(String full) {
    if (full == null || full.isEmpty() || !full.contains("/")) {
      throw new BotException("Expected a '/' separated pair (e.g. 'user/repo')");
    }
    return full.split(USER_REPO_SEPARATOR)[0];
  }

  public static String repo(String full) {
    if (full == null || full.isEmpty() || !full.contains("/")) {
      throw new BotException("Expected a '/' separated pair (e.g. 'user/repo')");
    }
    return full.split(USER_REPO_SEPARATOR)[1];
  }

  public static ParamBuilder params()  {
    return new Github.ParamBuilder();
  }

  public static class ParamBuilder {
    String creator;
    String assignee;
    String state;
    String since;

    public ParamBuilder() {
    }
    public ParamBuilder(String creator, String assignee, String state, String since) {
      ParamBuilder.this.creator = creator;
      ParamBuilder.this.assignee = assignee;
      ParamBuilder.this.state = state;
      ParamBuilder.this.since = since;
    } 

    public ParamBuilder withCreator(String creator) {
      return new ParamBuilder(creator, assignee, state, since);
    }

    public ParamBuilder withAssignee(String assignee) {
      return new ParamBuilder(creator, assignee, state, since);
    }

    public ParamBuilder state(String state) {
      return new ParamBuilder(creator, assignee, state , since);
    }

    public ParamBuilder closed() {
      return new ParamBuilder(creator, assignee, STATE_CLOSED , since);
    }

    public ParamBuilder lastWeek() {
      return new ParamBuilder(creator, assignee, state,
                              ZonedDateTime.now().minusWeeks(1).with(DayOfWeek.MONDAY).format(DateTimeFormatter.ofPattern(GITHUB_DATE_FORMAT_PATTERN)));
    }

    public ParamBuilder thisWeek() {
      ZonedDateTime.now().with(DayOfWeek.THURSDAY);
      ZonedDateTime.now().minusWeeks(1).with(DayOfWeek.THURSDAY).withHour(0);

      return new ParamBuilder(creator, assignee, state,
                              ZonedDateTime.now().with(DayOfWeek.MONDAY).format(DateTimeFormatter.ofPattern(GITHUB_DATE_FORMAT_PATTERN)));
    }

    public ParamBuilder sinceYesterday() {
      return new ParamBuilder(creator, assignee, state,
                              ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern(GITHUB_DATE_FORMAT_PATTERN)));
    }

    public Map<String, String> build() {

      Map<String, String> params = new HashMap<>();
      if (creator != null && !creator.isEmpty()) {
        params.put(CREATOR, creator);
      }

      if (assignee != null && !assignee.isEmpty()) {
        params.put(ASSIGNEE, assignee);
      }

      if (state != null && !state.isEmpty()) {
        params.put(STATE, state);
      }

      if (since != null && !since.isEmpty()) {
        params.put(SINCE, since);
      }
      return params;
    }
  }
  
}
