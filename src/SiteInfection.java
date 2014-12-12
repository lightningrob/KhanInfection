import java.io.*;
import java.util.*;


public class SiteInfection {
    
    public static void totalInfection(KAUser startUser, String infectVersion) {
    
        if (startUser == null) {
            throw new IllegalArgumentException("Null user");
        }
        
        Set<KAUser> connectedUsers = KAConnectionsManager.getInstance().getConnectedUsers(startUser, true);

        infectAndReport(connectedUsers, infectVersion);
    }
    
    public static boolean limitedInfection(KAUser startUser, String infectVersion, int targetLimit) {
        if (startUser == null) {
            throw new IllegalArgumentException("Null user");
        }
        
        KAConnectionsManager connMgr = KAConnectionsManager.getInstance();
        Set<KAUser> connectedUsers = new HashSet<KAUser>();
        // strategy: start going downhill, picking largest subgroups within limit
        connMgr.addConnectedUsers(startUser, true, connectedUsers, targetLimit);
        // then go uphill, at each point picking coaches with largest new learner subtree that fits
        
        // still have room? iterate through current set, find new coaches and repeat above set
        while (connectedUsers.size() < targetLimit) {
            int oldSize = connectedUsers.size();
            for (KAUser user: connectedUsers) {
                for (KAUser coach: user.getCoaches()) {
                    connMgr.addConnectedUsers(coach, true, connectedUsers, targetLimit);
                    if (connectedUsers.size() >= targetLimit)
                        break;
                }
                if (connectedUsers.size() >= targetLimit)
                    break;
            }
            // if no new users were added, no further progress can be made
            if (connectedUsers.size() == oldSize)
                break;
        }
        
        infectAndReport(connectedUsers, infectVersion);
        return true;
    }

    private static void infectAndReport(Set<KAUser> users, String infectVersion) {
        System.out.println("Infected following users:");
        for (KAUser user: users) {
            user.setSiteVersion(infectVersion);
            System.out.println(user.getUserName());
        }
    }
    public static void main(String args[]) {
        // args0: input file containing connections
        String usage = "Usage: SiteInfection <input file> startUserName [targetLimit#]";
        if (args.length < 2) {
            printErrorAndExit(usage);
            System.exit(1);
        }
        
        String fileName = args[0];
        String startUserName = args[1];
        int targetLimit = -1;
        if (args.length >= 3) {
            String limitString = args[2];
            try {
                targetLimit = Integer.valueOf(limitString);
            } catch (NumberFormatException e) {
                printErrorAndExit(usage);
            }
            if (targetLimit < 1) {
                printErrorAndExit("Target limit must be at least 1");
            }
        }

        File file = new File(fileName);
        if (! file.exists()) {
            printErrorAndExit("Cannot read input file - " + fileName + ".");
            System.exit(1);
        }
        
        //
        // Populate user directory and connections from input file
        //
        KAUserDirectory userDir = KAUserDirectory.getInstance();
        KAConnectionsManager connMgr = KAConnectionsManager.getInstance();

        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufReader = new BufferedReader(reader);
            while (true) {
                String inputLine = bufReader.readLine();
                if (inputLine == null)
                    break;
                // split line into coach learner pair
                String pair[] = inputLine.split(" ");
                if (pair.length < 2) {
                    printErrorAndExit("Invalid input line: " + inputLine);
                }
                String coachName = pair[0];
                String learnerName = pair[1];
                KAUser coachUser = userDir.getOrCreateUserByName(coachName);
                KAUser learnerUser = userDir.getOrCreateUserByName(learnerName);
                connMgr.addCoachingRelationship(coachUser, learnerUser);
            }
        } catch (FileNotFoundException e) {
            printErrorAndExit("Cannot read input file - " + fileName + ".");
        } catch (IOException e) {
            printErrorAndExit("Error reading input file - " + fileName + ".");
        }
        
        // Make sure user is valid
        KAUser startUser = userDir.getUserByName(startUserName);
        if (startUser == null) {
            printErrorAndExit("User not found in input file: " + startUserName);
        }
        
        // Make up the version
        String infectVersion = "Best version ever";
        if (targetLimit > 0) {
            limitedInfection(startUser, infectVersion, targetLimit);
        } else {
            totalInfection(startUser, infectVersion);
        }
    }
    
    private static void printErrorAndExit(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
