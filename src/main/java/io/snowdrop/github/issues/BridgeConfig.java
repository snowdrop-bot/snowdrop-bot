package io.snowdrop.github.issues;

import java.util.Set;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "github.bridge")
public class BridgeConfig {

  private Set<String> users;
  private Set<String> sourceRepos;
  private String targetOrganization;
  private String terminalLabel;


  @Override
  public String toString() {
    return "BridgeConfig [sourceRepos=" + sourceRepos + ", targetOrganization=" + targetOrganization
      + ", terminalLabel=" + terminalLabel + ", users=" + users + "]";
  }


  public Set<String> getUsers() {
    return users;
  }


  public void setUsers(Set<String> users) {
    this.users = users;
  }


  public Set<String> getSourceRepos() {
    return sourceRepos;
  }


  public void setSourceRepos(Set<String> sourceRepos) {
    this.sourceRepos = sourceRepos;
  }


  public String getTargetOrganization() {
    return targetOrganization;
  }


  public void setTargetOrganization(String targetOrganization) {
    this.targetOrganization = targetOrganization;
  }


  public String getTerminalLabel() {
    return terminalLabel;
  }


  public void setTerminalLabel(String terminalLabel) {
    this.terminalLabel = terminalLabel;
  }


}
