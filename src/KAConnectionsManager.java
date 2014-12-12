import java.util.*;

public class KAConnectionsManager {

    private Map<KAUser, Set<KAUser>> _coachToLernerMapping = new HashMap<KAUser, Set<KAUser>>();
    private Map<KAUser, Set<KAUser>> _lernerToCoachMapping = new HashMap<KAUser, Set<KAUser>>();

    // singleton pattern
    private static KAConnectionsManager _instance = new KAConnectionsManager();
    private KAConnectionsManager() {}
    public static KAConnectionsManager getInstance() { return _instance; }

    public synchronized void addCoachingRelationship(KAUser coachUser, KAUser lernerUser) {
        Set<KAUser> lernerSet = getOrCreateUserSet(_coachToLernerMapping, coachUser);
        lernerSet.add(lernerUser);
        
        Set<KAUser> coachSet = getOrCreateUserSet(_lernerToCoachMapping, lernerUser);
        coachSet.add(coachUser);
    }
    public synchronized void removeCoachingRelationship(KAUser coachUser, KAUser lernerUser) {
        Set<KAUser> lernerSet = getOrCreateUserSet(_coachToLernerMapping, coachUser);
        lernerSet.remove(lernerUser);
        
        Set<KAUser> coachSet = getOrCreateUserSet(_lernerToCoachMapping, lernerUser);
        coachSet.remove(coachUser);
    }
    
    private Set<KAUser> getOrCreateUserSet(Map<KAUser, Set<KAUser>> mapping, KAUser user) {
        Set<KAUser> userSet = mapping.get(user);
        if (userSet == null) {
            userSet = new HashSet<KAUser>();
            mapping.put(user, userSet);
        }
        return userSet;
    }

    public synchronized Set<KAUser> getCoachesForUser(KAUser user) {
        return getOrCreateUserSet(_lernerToCoachMapping, user);
    }
    
    public synchronized Set<KAUser> getLernersCoachedByUser(KAUser user) {
        return getOrCreateUserSet(_coachToLernerMapping, user);
    }


    // Unlimited version
    public Set<KAUser> getConnectedUsers(KAUser startUser, boolean bothDirections) {
        Set<KAUser> connectedUsers = new HashSet<KAUser>();
        addConnectedUsers(startUser, bothDirections, connectedUsers);
        return connectedUsers;
    }
    
    private void addConnectedUsers(KAUser user, boolean bothDirections, Set<KAUser> connectedUsers) {
        if (connectedUsers.contains(user)) {
            // already been here
            return;
        }
        connectedUsers.add(user);
        // Debugging to illustrate traversal
        System.out.println("Added user " + user.getUserName());

        for (KAUser adjacentUser: getLernersCoachedByUser(user)) {
            addConnectedUsers(adjacentUser, bothDirections, connectedUsers);
        }
        if (bothDirections) {
            for (KAUser adjacentUser: getCoachesForUser(user)) {
                addConnectedUsers(adjacentUser, bothDirections, connectedUsers);
            }
        }
    }

    /**
     * Returns the set of new users in the connected set of the specified user that are not already contained
     * in the existingUsers set.
     * @param startUser the user to begin with
     * @param bothDirections true iff considering both coaching and coached-by relationships
     * @param existingUsers the set of already connected users
     * @param targetLimit the total user count desired
     * @return the set of new users visited
     */
    public Set<KAUser> getConnectedUsers(KAUser startUser, boolean bothDirections, int targetLimit) {
        Set<KAUser> existingUsers = new HashSet<KAUser>();
        addConnectedUsers(startUser, bothDirections, existingUsers, targetLimit);
        return existingUsers;
    }
    
    // Notes on possible optimizations:
    // infection gives priority to instructor or to coach with highest IScore
    // NCLB: if only subset of instructor's students can be infected, preference to lowest IScore
    // At each iteration (BFS), given remaining count, check how many new students would get infected
    // If targetCount > class size, infect entire classrooms that sum closest to target
    // If count < class size, infect largest cluster(s) closest to count
    
    // Simple greedy algorithm: add until hit limit
    public void addConnectedUsers(KAUser user, boolean bothDirections, Set<KAUser> existingUsers, int targetLimit) {
        if (existingUsers.size() >= targetLimit) {
            // already hit the limit
            return;
        }
        if (existingUsers.contains(user)) {
            // already been here
            return;
        }
        existingUsers.add(user);
        // Debugging to illustrate traversal
        System.out.println("Added user " + user.getUserName());
        // head downhill  (toward learners) first
        for (KAUser learner: getLernersCoachedByUser(user)) {
            addConnectedUsers(learner, false, existingUsers, targetLimit);
            if (existingUsers.size() >= targetLimit)
                break;
        }
        if (bothDirections) {
            for (KAUser adjacentUser: getCoachesForUser(user)) {
                addConnectedUsers(adjacentUser, bothDirections, existingUsers, targetLimit);
            }
        }
    }

}
