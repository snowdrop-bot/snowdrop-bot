
package io.snowdrop.github.issues;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Assignees implements Serializable {

  private static final long serialVersionUID = -3006631826215200311L;
  public static final String ASSIGNEES = "assignees";
  
  private String[] assignees;

  public Assignees(String assignee) {
    this.assignees = new String[1];
    this.assignees[0] = assignee;
  }

  public Assignees(String[] assignees) {
    this.assignees = assignees;
  }

  public String[] getAssignees() {
    return assignees;
  }

  public void setAssignees(String[] assignees) {
    this.assignees = assignees;
  }

  public Map<Object, Object> toMap() {
    Map<Object, Object> result = new HashMap<>();
    result.put(ASSIGNEES, assignees);
    return result;
  }
  
}
