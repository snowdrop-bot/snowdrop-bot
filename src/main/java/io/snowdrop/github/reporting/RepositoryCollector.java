package io.snowdrop.github.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.snowdrop.BotException;
import io.snowdrop.github.reporting.model.Repository;

public class RepositoryCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryCollector.class);
  private static final SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy");

  private final GitHubClient client;
  private final RepositoryService repositoryService;

  private final Set<String> users;
  private final Set<String> organizations;

  private final Map<String, Set<Repository>> repositories = new HashMap<>();

  public RepositoryCollector(GitHubClient client, Set<String> users, Set<String> organizations) {
    this.client = client;
    this.repositoryService = new RepositoryService(client);
    this.users = users;
    this.organizations = organizations;
    init();
  }

  public void init() {
    users.stream().forEach(u -> {
      repositories.put(u, new HashSet<>());
    });
  }

  public void refresh() {
    LOGGER.info("Refreshing reporting data.");
    collectForks();
  }

  public Map<String, Set<Repository>> collectForks() {
    users.stream().forEach(u -> {
      LOGGER.info("Getting forks for user: {}.", u);
      Set<Repository> forks = userForks(u);
      repositories.get(u).addAll(forks);
      LOGGER.info("User: {} forks: [{}].", u, forks.stream().map(r -> r.getName()).collect(Collectors.joining(",")));
    });
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

  public Map<String, Set<Repository>> getRepositories() {
    return repositories;
  }

}
