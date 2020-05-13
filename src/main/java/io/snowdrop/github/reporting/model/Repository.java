package io.snowdrop.github.reporting.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

@Entity
@IdClass(ForkId.class)
public class Repository extends PanacheEntityBase {

    @Id
    private String url;
    @Id
    private String parent;

    private String owner;
    private String name;
    private boolean fork;

    public Repository() {
    }

    public Repository(String url, String parent, String owner, String name, boolean fork) {
        this.url = url;
        this.owner = owner;
        this.name = name;
        this.fork = fork;
        this.parent = parent;
    }

    public static Repository fromFork(String owner, String user, String repo) {
        return new Repository("https://github.com" + owner + "/" + repo, user + "/" + repo, owner, repo, true);
    }

    public static Repository create(org.eclipse.egit.github.core.Repository repo) {
        return new Repository(repo.getHtmlUrl(),
                repo.getParent() != null ? repo.getParent().getOwner().getLogin() + "/" + repo.getParent().getName() : Parent.NONE,
                repo.getOwner().getLogin(), repo.getName(),
                repo.isFork());
    }

    /**
     * <p>Issues modified between a date range for a specific repository.</p>
     *
     * @param prepoOwner
     * @param prepoName
     * @return
     */
    public static PanacheQuery<PanacheEntityBase> findByExcOwnerName(final String prepoOwner, final String prepoName) {
        //    return Repository.find("owner != ?1 AND name != ?2", prepoOwner, prepoName);
        return Repository.find(" name != ?1", prepoName);
    }

    /**
     * <p>Issues modified between a date range for a specific repository.</p>
     *
     * @param prepoOwner
     * @param prepoName
     * @return
     */
    public static PanacheQuery<PanacheEntityBase> findByOwnerName(final String prepoOwner, final String prepoName) {
        return Repository
                .find("owner = ?1 AND name = ?2", prepoOwner, prepoName);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public boolean hasParent() {
        return !Parent.NONE.equals(parent);
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((url == null) ? 0 : url.hashCode());
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
        Repository other = (Repository) obj;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

}
