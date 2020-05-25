package io.snowdrop.github.reporting.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PullRequestTest {

  @Test
  public void testIsValidIssueUrl() throws Exception {
    assertFalse(PullRequest.isValidIssueUrl(null));
    assertFalse(PullRequest.isValidIssueUrl(""));
    assertFalse(PullRequest.isValidIssueUrl("http:/some.url"));
    assertTrue(PullRequest.isValidIssueUrl(" https://api.github.com/repos/someorg/someproject/issues/100"));
  }
}

