package io.snowdrop.github.issues;

import java.util.Set;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "github.bridge")
public class BridgeConfig {

  Set<String> sourceRepos;
  String targetOrganization;
  LabelConfig autoLabel;
  LabelConfig terminalLabel;

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

  public LabelConfig getAutoLabel() {
    return autoLabel;
  }

  public void setAutoLabel(LabelConfig autoLabel) {
    this.autoLabel = autoLabel;
  }

  public LabelConfig getTerminalLabel() {
    return terminalLabel;
  }

  public void setTerminalLabel(LabelConfig terminalLabel) {
    this.terminalLabel = terminalLabel;
  }
  
}
