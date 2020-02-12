package io.snowdrop.github.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.StatusLogger;
import io.snowdrop.github.Github;
import io.snowdrop.github.reporting.model.Parent;
import io.snowdrop.github.reporting.model.Repository;

public class RepositoryCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryCollector.class);
  private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  private final GitHubClient client;
  private final StatusLogger forkLogger;
  private final StatusLogger repositoryLogger;
  private final RepositoryService repositoryService;

  private final Set<String> users;
  private final Set<String> organizations;
  private final Set<String> additionalRepositories;

  private final Map<String, Set<Repository>> forks = new HashMap<>();
  private final Set<Repository> repositories = new HashSet<>();

  public RepositoryCollector(GitHubClient client, StatusLogger forkLogger, StatusLogger repositoryLogger,  Set<String> users, Set<String> organizations, Set<String> additionalRepositories) {
    this.client = client;
    this.forkLogger  = forkLogger;
    this.repositoryLogger = repositoryLogger;
    this.repositoryService = new RepositoryService(client);
    this.users = users;
    this.organizations = organizations;
    this.additionalRepositories = additionalRepositories;
    init();
  }

  public void init() {
    users.stream().forEach(u -> {
      forks.put(u, new HashSet<>());
    });
  }

  public void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectForks();
  }

  public Map<String, Set<Repository>> collectForks() {
    users.stream()
      .map(forkLogger.log(users.size(), "Collecting forks for user %s.", u -> u))
      .forEach(u -> {
      LOGGER.info("Getting forks for user: {}.", u);
      Set<Repository> f = userForks(u);
      forks.get(u).addAll(f);
      LOGGER.info("User: {} forks: [{}].", u, f.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
    });
    return forks;
  }

  public Set<Repository> collectRepositories() {
   repositories.addAll(additionalRepositories.stream()
                       .map(r -> repository(Github.user(r), Github.repo(r))).map(Repository::create)
                       .map(repositoryLogger.log(users.size(), "Collecting repository %s.", r -> r.getUrl()))
                       .collect(Collectors.toSet()));
   return repositories;
  }

  /**
   * Get all the repositories of the specified user.
   *
   * @param user The user
   * @return A set of {@link Repository}.
   */
  public Set<Repository> userForks(final String user) {
    synchronized (client) {
      try {
        return repositoryService.getRepositories(user).stream().filter(r -> r.isFork())
            .map(r -> repository(user, r.getName()))
            .filter(r -> organizations.contains(r.getParent().getOwner().getLogin())).map(Repository::create)
            .collect(Collectors.toSet());
      } catch (final IOException e) {
        throw BotException.launderThrowable(e);
      }
    }
  }

  private org.eclipse.egit.github.core.Repository repository(String user, String name) {
    synchronized (client) {
      try {
        return repositoryService.getRepository(user, name);
      } catch (IOException e) {
        throw BotException.launderThrowable("Error reading repository:" + user + "/" + name, e);
      }
    }
  }

  public Set<String> getUsers() {
    return users;
  }

  public Set<String> getOrganizations() {
    return organizations;
  }

  public Stream<Repository> getAllRepositories() {
    return Repository.<Repository>streamAll();
  }

  public Stream<Repository> getRepositories() {
    return Repository.<Repository>streamAll().filter(r -> r.getParent().equals(Parent.NONE));
  }

  public Stream<Repository> getForks() {
    return Repository.<Repository>streamAll().filter(r -> !r.getParent().equals(Parent.NONE));
  }
}
