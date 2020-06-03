package io.snowdrop.reporting.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class Associate extends PanacheEntityBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(Associate.class);

  @Id
  String associate;
  String alias;
  String source;
  String name;

  public Associate() {
  }

  public Associate(final String associate, final String alias, final String source, final String name) {
    this.associate = associate;
    this.alias = alias;
    this.source = source;
    this.name = name;
  }

  public static Associate create(final String associate, final String alias, final String source, final String name) {
    return new Associate(associate, alias, source, name);
  }

  /**
   * <p>Gets the name of an associate.</p>
   * @param associate
   * @param issueSource
   * @return
   */
  public static String getAssociateName(final String associate, final IssueSource issueSource) {
    String associateName;
    Associate storedAssociate = Associate.findById(associate);
    if (storedAssociate != null) {
      associateName = storedAssociate.getName();
    } else {
      associateName = associate;
    }
    return associateName;
  }

  public String getAssociate() {
    return associate;
  }

  public void setAssociate(final String associate) {
    this.associate = associate;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(final String alias) {
    this.alias = alias;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((associate == null) ? 0 : associate.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Associate other = (Associate) obj;
    if (associate == null) {
      if (other.associate != null)
        return false;
    } else if (!associate.equals(other.associate))
      return false;
    return true;
  }

  @Override public String toString() {
    StringBuffer sb = new StringBuffer("{associate: ").append(associate).append(", alias: ").append(alias).append(", source: ").append(source)
    .append(", name: ").append(name).append("}");
    return sb.toString();
  }
}
