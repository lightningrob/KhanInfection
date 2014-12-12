import java.util.Set;

public class KAUser {
    // attributes
    private String _userName;
    // Version would ideally be its own class with attributes like features, major/minor, testingLevel
    // But keeping it simple here
    private String _siteVersion;
    
    public KAUser(String userName) {
        _userName = userName;
    }
    
    public String getUserName() { return _userName; }
    public String getSiteVersion() { return _siteVersion; }
    public void setSiteVersion(String version) { _siteVersion = version; }
    
    public Set<KAUser> getCoachedUsers() {
        return KAConnectionsManager.getInstance().getLernersCoachedByUser(this);
    }

    public Set<KAUser> getCoaches() {
        return KAConnectionsManager.getInstance().getCoachesForUser(this);
    }

    public String toString() { return _userName; }

    public int hashCode() { return _userName.hashCode(); }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof KAUser) {
            KAUser user = (KAUser) o;
            return (this._userName.equals(user._userName));
        }
        return false;
    }
    
}
