import java.util.Map;
import java.util.HashMap;

public class KAUserDirectory {

    private Map<String, KAUser> _nameToUserMap = new HashMap<String, KAUser>();
    
    // singleton pattern
    private static KAUserDirectory _instance = new KAUserDirectory();
    private KAUserDirectory() {}
    public static KAUserDirectory getInstance() { return _instance; }

    public synchronized void addUser(String userName) {
        _nameToUserMap.put(userName, new KAUser(userName));
    }
    public synchronized void addUser(KAUser user) {
        _nameToUserMap.put(user.getUserName(), user);
    }
    public synchronized KAUser getUserByName(String userName) {
        return _nameToUserMap.get(userName);
    }
    public synchronized KAUser getOrCreateUserByName(String userName) {
        KAUser user = _nameToUserMap.get(userName);
        if (user == null) {
            user = new KAUser(userName);
            _nameToUserMap.put(userName, user);
        }
        return user;
    }

}
