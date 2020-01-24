package io.snowdrop.github.issues;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.IGitHubConstants;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.Github;

public class GithubIssueBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger(GithubIssueBridge.class);

  public static final String SEPARATOR = "\n\n---\n\n";
  public static final String UPSTREAM_REPO_PREFIX = "$upstream:";
  public static final String UPSTREAM_REPO_SUFFIX = "$";

  public static final String ASSIGNEES = "assignees";

  private final GitHubClient client;
  private final IssueService issueService;
  private final LabelService labelService;

  private final String sourceRepository;
  private final String targetRepository;
  private final String terminalLabel;
  private final Set<String> users;

  private final Map<String, Map<String, Label>> repoLables = new HashMap<>();

  private final Map<Integer, Issue> openIssues = new HashMap<>();
  private final Map<Integer, Issue> closedIssues = new HashMap<>();
  private final Map<Integer, Issue> downstreamIssues = new HashMap<>();

  public GithubIssueBridge(GitHubClient client, String sourceRepository, String targetRepository, String terminalLabel, Set<String> users) {
    this.client = client;
    this.issueService = new IssueService(client);
    this.labelService = new LabelService(client);
    this.sourceRepository = sourceRepository;
    this.targetRepository = targetRepository;
    this.terminalLabel = terminalLabel;
    this.users = users;
  }

  public void init() {
    LOGGER.info("Initializing.");
    refresh();
  }

  public void refresh() {
    LOGGER.info("Refershing bridge: {} -> {}.", sourceRepository, targetRepository);
    downstreamOpenIssues().stream().forEach(i -> downstreamIssues.put(i.getNumber(), i));

    teamOpenIssues().stream().forEach(i -> openIssues.put(i.getNumber(), i));
    teamClosedIssues().stream().forEach(i -> closedIssues.put(i.getNumber(), i));
  }

  public List<Issue> cloneTeamIssues() {
    LOGGER.info("Cloning team issues from {}.", sourceRepository);
    return openIssues.values().stream().filter(i -> !findDownstreamIssue(i).isPresent())
        .map(i -> cloneIssue(i, targetRepository)).collect(Collectors.toList());
  }

  public void assignTeamIssues() {
    LOGGER.info("Assigning team issues in {}.", targetRepository);
    downstreamIssues.values().stream().map(i -> new AbstractMap.SimpleEntry<>(i, findUpstreamIssue(i)))
        .filter(e -> e.getValue().isPresent()).forEach(e -> assign(client, targetRepository, e.getKey(),
            e.getValue().map(Issue::getAssignee).orElseThrow(IllegalStateException::new)));
  }

  public List<Issue> closeTeamIssues() {
    LOGGER.info("Closing team issues from {}.", sourceRepository);
    return closedIssues.values().stream().map(i -> findDownstreamIssue(i)).filter(o -> o.isPresent()).map(Optional::get)
        .map(i -> markIssueAsClosed(i, targetRepository)).collect(Collectors.toList());
  }

  /**
   * Stream all the open issues of the source repository assigned to team mebmers.
   *
   * @return A stream of issues.
   */
  public List<Issue> teamOpenIssues() {
    return source().filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
        .collect(Collectors.toList());
  }

  /**
   * Stream all the open issues of the source repository assigned to team mebmers.
   *
   * @return A stream of issues.
   */
  public List<Issue> teamClosedIssues() {
    return source(Github.params().closed().build())
        .filter(i -> i.getAssignee() != null && users.contains(i.getAssignee().getLogin()))
        .collect(Collectors.toList());
  }

  /**
   * Stream all the open issues of the source repository assigned to team mebmers.
   *
   * @return A stream of issues.
   */
  public List<Issue> downstreamOpenIssues() {
    return target().filter(i -> "open".equals(i.getState())).collect(Collectors.toList());
  }

  /**
   * Find a downstream issue that correlates with the specified issue.
   *
   * @param issue the issue to look up.
   * @return An optional containing the downstream issue.
   */
  private Optional<Issue> findDownstreamIssue(Issue issue) {
    LOGGER.info("Checking if downstream issue {} exists downstream.", issue.getNumber());
    String link = UPSTREAM_REPO_PREFIX + issue.getNumber() + UPSTREAM_REPO_SUFFIX;
    return downstreamIssues.values().stream()
        // .map(i -> {System.out.println(issue.getNumber() + " :" + i.getTitle());
        // return i;})
        .filter(i -> i.getBody() != null && i.getBody().contains(link)).findFirst();
  }

  /**
   * Find a downstream issue that correlates with the specified issue.
   *
   * @param issue the issue to look up.
   * @return An optional containing the downstream issue.
   */
  private Optional<Issue> findUpstreamIssue(Issue issue) {
    if (issue == null) {
      throw new IllegalArgumentException("Issue cannot be null.");
    }

    LOGGER.info("Checking if upstream issue {} exists in upstream repository.", issue.getNumber());
    if (issue.getBody() == null || issue.getBody().isEmpty()) {
      return Optional.empty();
    }

    Integer number = upstreamIssueNumber(issue.getBody());
    LOGGER.info("Checking upstream for issue {}.", number);
    return Optional.ofNullable(openIssues.get(number));
  }

  private Label getLabel(String repo, String label) {
    synchronized (client) {
      Map<String, Label> labelCache = repoLables.computeIfAbsent(repo, l -> new HashMap<String, Label>());
      return labelCache.computeIfAbsent(label, l -> {
        try {
          return labelService.getLabel(Github.user(repo), Github.repo(repo), label);
        } catch (IOException e) {
          throw BotException.launderThrowable(e);
        }
      });
    }
  }

  /**
   * Creates an issue to the target repository.
   *
   * @param issue the issue to clone.
   * @param repo  the target repository.
   */
  private Issue cloneIssue(Issue issue, String repo) {
    synchronized (client) {
      try {
        LOGGER.info("Cloning issue {} to repository: {}.", issue.getNumber(), repo);
        Issue cloned = new Issue();
        cloned.setTitle(issue.getTitle());
        cloned.setAssignee(issue.getAssignee());
        cloned.setBody(issue.getBody() + SEPARATOR + issue.getHtmlUrl() + SEPARATOR + UPSTREAM_REPO_PREFIX
            + issue.getNumber() + UPSTREAM_REPO_SUFFIX);
        Issue created = issueService.createIssue(Github.user(repo), Github.repo(repo), cloned);
        if (issue.getAssignee() != null && created.getAssignee() == null) {
          LOGGER.info("Setting assignee: {} on issue: {}.", issue.getAssignee().getLogin(), created.getNumber());
          assign(client, repo, created, issue.getUser());
          return cloned;
        }
        return created;
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  /**
   * Mark an issue as closed.
   *
   * @param upstream the issue to clone.
   * @param repo     the target repository.
   */
  private Issue markIssueAsClosed(Issue issue, String repo) {
    synchronized (client) {
      try {
        LOGGER.info("Closing issue {} to repository: {}.", issue.getNumber(), repo);
        List<Label> labels = new ArrayList<>(issue.getLabels());
        labels.add(getLabel(repo, terminalLabel));
        issue.setLabels(labels);
        return issueService.editIssue(Github.user(repo), Github.repo(repo), issue);
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  /**
   * Find an issue by number in the target repository.
   *
   * @param repo   the target repository
   * @param number the issue number
   * @return An optional of the issue.
   */
  private Optional<Issue> findIssue(String repo, String number) {
    synchronized (client) {
      try {
        return Optional.of(issueService.getIssue(Github.user(repo), Github.repo(repo), number));
      } catch (IOException e) {
        throw new BotException(e);
      }
    }
  }

  private Stream<Issue> stream(String repo, Map<String, String> params) {
    synchronized (client) {
      try {
        return issueService.getIssues(Github.user(repo), Github.repo(repo), params).stream();
      } catch (IOException e) {
        throw new BotException(e);
      }
    }
  }

  private Stream<Issue> source(Map<String, String> params) {
    return stream(sourceRepository, params);
  }

  private Stream<Issue> source() {
    return source(new HashMap<>());
  }

  private Stream<Issue> target(Map<String, String> params) {
    return stream(targetRepository, params);
  }

  private Stream<Issue> target() {
    return target(new HashMap<>());
  }

  //
  // Utilities
  //

  private synchronized static User assign(GitHubClient client, String repo, Issue issue, User user) {
    synchronized (client) {
      if (issue == null)
        throw new IllegalArgumentException("Issue cannot be null"); //$NON-NLS-1$
      if (user == null)
        throw new IllegalArgumentException("Issue user cannot be null"); //$NON-NLS-1$

      StringBuilder uri = new StringBuilder(IGitHubConstants.SEGMENT_REPOS);
      uri.append('/').append(Github.user(repo));
      uri.append('/').append(Github.repo(repo));
      uri.append(IGitHubConstants.SEGMENT_ISSUES);
      uri.append('/').append(issue.getNumber());
      uri.append('/').append(ASSIGNEES);
      GitHubRequest request = new GitHubRequest();
      request.setUri(uri);
      try {
        return client.post(uri.toString(), new Assignees(user.getLogin()).toMap(), User.class);
      } catch (IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  private static Integer upstreamIssueNumber(String body) {
    if (body == null || body.isEmpty()) {
      throw new IllegalStateException("Issue body should not be null or empty.");
    }
    if (body.contains(UPSTREAM_REPO_PREFIX)) {
      int start = body.lastIndexOf(UPSTREAM_REPO_PREFIX);
      int end = body.lastIndexOf(UPSTREAM_REPO_SUFFIX);
      if (end < start) {
        throw new IllegalStateException("Issue reference is malformed. No suffix found.");
      }
      return Integer.parseInt(body.substring(start + UPSTREAM_REPO_PREFIX.length(), end));
    }
    throw new IllegalStateException("Issue body should contain correlation info.");
  }

}
