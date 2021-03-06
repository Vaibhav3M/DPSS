package Replica_Faulty.Server.America;

import GameServer_CORBA.GameServerPOA;
import Replica_Faulty.Constants.Constants;
import Replica_Faulty.Model.Player;
import Replica_Faulty.SendUDP.FIFOUDPMessage;
import org.omg.CORBA.ORB;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class F_AmericanServerImpl extends GameServerPOA {

    static boolean isLeader = Constants.isLeader;

    private ORB orb;
    private static final long serialVersionUID = 7526472295622776147L;

    //lock to enable synchronization
    private static Lock lock = new ReentrantLock(true);
    //to store player info
    private static Hashtable<Character, ArrayList<Player>> playersTable = new Hashtable<>();
    //to log activities in a log file
    private static Logger LOGGER;

    private static boolean run = false;

    protected F_AmericanServerImpl(Logger logger, boolean run1) {
        super();
        run = run1;
        LOGGER = logger;
        addDummyData(run);
    }

    // set ORB value
    public void setORB(ORB orb_val) { orb = orb_val; }

    @Override
    public String createPlayerAccount(String FirstName, String LastName, float Age, String Username, String Password, String IPAddress) {

        String result = "Successful";
        String response1 = "";
        String response2 = "";

        if (isLeader) {
            String action = "1:" + FirstName  + ":" + LastName + ":" + Math.round(Age) + ":" + Username  + Password;
            response1 = generateUDPResponse(Constants.L_SERVER_PORT_AMERICA, action);
            response2 = generateUDPResponse(Constants.R2_SERVER_PORT_AMERICA, action);
        }

        boolean isFromServerIP = (Integer.parseInt(IPAddress) == Constants.SERVER_IP_AMERICA);

        //create player object
        Player player = new Player(FirstName, LastName, Math.round(Age) , Username+"Fault", Password, String.valueOf(Constants.SERVER_IP_AMERICA), false);

        LOGGER.info("Received   request - Create Player - " + player.toString());

        if (isFromServerIP) {
            //check if username exists
            if (checkUserName(player.getUserName())) {

                LOGGER.info("Username=" + player.getUserName() + " already existed");
                return "Username already exists";
            }
        }

        char playerKey = player.getUserName().charAt(0);
        ArrayList<Player> playerList;

        try {
            // lock while performing operations
            lock.lock();

            if (playersTable.containsKey(playerKey)) {

                playerList = playersTable.get(playerKey);

                for (int i = 0; i < playerList.size(); i++) {
                    Player currPlayer = playerList.get(i);

                    if (currPlayer.getUserName().equalsIgnoreCase(player.getUserName())) {
                        LOGGER.info("Username=" + player.getUserName() + " already existed");

                        return "UserName already exists";
                    }
                }
                playerList.add(player);
            } else {
                playerList = new ArrayList<>();
                playerList.add(player);
                playersTable.put(playerKey, playerList);
            }
        } finally {
            //unlock once operation complete
            lock.unlock();
        }

        LOGGER.info("Player Created successfully - " + player.toString());

        if (isLeader) {
            result = Constants.calculateEndResult(result, response1, response2, Constants.SERVER_PORT_AMERICA);
            System.out.println("L: " + result + " | R2: " + response1 + " | R3:  " + response2);
        }
        return result;
    }


    @Override
    public String playerSignIn(String Username, String Password, String IPAddress) {

        LOGGER.info("Received   request - SignIn Player - " + "Username=" + Username);

        String result = Username + " not found";
        String response1 = "";
        String response2 = "";

        if (isLeader) {
            String action = "2:" + Username + ":" + Password + ":" + IPAddress;
            response1 = generateUDPResponse(Constants.L_SERVER_PORT_AMERICA, action);
            response2 = generateUDPResponse(Constants.R2_SERVER_PORT_AMERICA, action);
        }

        char playerKey = Username.charAt(0);

        try {
            // lock while performing operations
            lock.lock();
            if (playersTable.containsKey(playerKey)) {

                ArrayList<Player> playerList = playersTable.get(playerKey);

                for (int i = 0; i < playerList.size(); i++) {
                    Player currPlayer = playerList.get(i);
                    if (currPlayer.getUserName().equalsIgnoreCase(Username) && currPlayer.getPassword().equalsIgnoreCase(Password)) {

                        if (currPlayer.isSignedIn()) {
                            LOGGER.info("Player is already SignedIn - " + "Username=" + Username);
                            return currPlayer.getUserName() + " is already logged in.";
                        }

                        currPlayer.setSignedIn(true);
                        playerList.remove(i);
                        playerList.add(currPlayer);
                        playersTable.put(playerKey, playerList);

                        LOGGER.info("Player SignedIn - " + "Username=" + Username);
                        result = currPlayer.getUserName() + " has logged in.";
                    }
                    break;
                }
            } else {

                LOGGER.info("Player not found - " + "Username=" + Username);
                result = Username + " not found";
            }
        } finally {
            lock.unlock();
        }

        if (isLeader) {
                result = Constants.calculateEndResult(result, response1, response2, Constants.SERVER_PORT_AMERICA);
            System.out.println("L: " + result + " | R2: " + response1 + " | R3:  " + response2);
        }

        return result;
        //return Username + " not found";
    }

    @Override
    public String playerSignOut(String Username, String IPAddress) {
        boolean isFromServerIP = (Integer.parseInt(IPAddress) == Constants.SERVER_IP_AMERICA);

        String result = "User not found";
        String response1 = "";
        String response2 = "";

        if (isLeader) {
            String action = "3:" + Username  + ":" + IPAddress;
            response1 = generateUDPResponse(Constants.L_SERVER_PORT_AMERICA, action);
            response2 = generateUDPResponse(Constants.R2_SERVER_PORT_AMERICA, action);
        }

        char playerKey = Username.charAt(0);

        try {
            // lock while performing operations
            lock.lock();
            if (playersTable.containsKey(playerKey)) {

                ArrayList<Player> playerList = playersTable.get(playerKey);

                for (int i = 0; i < playerList.size(); i++) {
                    Player currPlayer = playerList.get(i);
                    if (currPlayer.getUserName().equalsIgnoreCase(Username)) {

                        if (isFromServerIP) {
                            LOGGER.info("Received   request - SignOut Player - " + Username);

                            if (!currPlayer.isSignedIn()) {
                                LOGGER.info("Player is not SignedIn - " + "Username=" + Username);
                                result = currPlayer.getUserName() + " is not signed in.";
                            }
                            currPlayer.setSignedIn(false);
                            playerList.remove(i);
                            playerList.add(currPlayer);
                            playersTable.put(playerKey, playerList);

                        }
                        result = currPlayer.getUserName() + " has logged out.";

                        LOGGER.info("Player SignedOut - " + "Username=" + Username);
                        break;
                    }
                }
            } else {
                LOGGER.info("Player not found - " + "Username=" + Username);
                result = "User not found";
            }
        } finally {
            lock.unlock();
        }

        if (isLeader) {
                result = Constants.calculateEndResult(result, response1, response2, Constants.SERVER_PORT_AMERICA);
            System.out.println("L: " + result + " | R2: " + response1 + " | R3:  " + response2);
        }

        return result;
        //return "User not found";
    }

    @Override
    public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress, boolean checkOtherServers) {
        if (!AdminUsername.equalsIgnoreCase("Admin") || !AdminPassword.equalsIgnoreCase("Admin")) {
            return "Username or password incorrect.";
        }
            String response = "NA: ";
            int onlineCount = 0;
            int offlineCount = 0;
            try {
                lock.lock();
                for (char key : playersTable.keySet()) {
                    for (Player p : playersTable.get(key)) {
                        if (p.isSignedIn()) onlineCount++;
                        else offlineCount++;
                    }
                }
            } finally {
                lock.unlock();
            }

            String response_Asia = "";
            String response_Europe = "";

            //Send UDP requests to other servers
            if (checkOtherServers) {
                response_Asia = generateUDPResponse(Constants.SERVER_PORT_ASIA,"6");
                response_Europe = generateUDPResponse(Constants.SERVER_PORT_EUROPE,"6");
            }

            //append the results
            response = response + onlineCount + " online, " + offlineCount + " offline. " + response_Asia + response_Europe;
            return response;
        }

    @Override
    public String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress) {

        LOGGER.info("Received request - Transfer Player - " + "Username= " + Username + " OldIP: " + OldIPAddress + " NewIP: " +  NewIPAddress);

        if(OldIPAddress.equalsIgnoreCase(NewIPAddress)) return "New IP and Old IP must be different";

        String result = "User not found";
        String response1 = "";
        String response2 = "";

        if (isLeader) {
            String action = "5:" + Username  + ":" + Password + ":" + OldIPAddress + ":" + NewIPAddress;
            response1 = generateUDPResponse(Constants.L_SERVER_PORT_AMERICA, action);
            response2 = generateUDPResponse(Constants.R2_SERVER_PORT_AMERICA, action);
        }

        char playerKey = Username.charAt(0);

        try {
            // lock while performing operations
            lock.lock();
            if (playersTable.containsKey(playerKey)) {

                ArrayList<Player> playerList = playersTable.get(playerKey);

                for (int i = 0; i < playerList.size(); i++) {
                    Player currPlayer = playerList.get(i);
                    if (currPlayer.getUserName().equalsIgnoreCase(Username)) {

                        int newServerPort = Constants.getServerPortFromIP(Integer.parseInt(NewIPAddress));
                        String playerInfo = currPlayer.getFirstName() + ":" + currPlayer.getLastName() + ":" + currPlayer.getAge() + ":" + currPlayer.getUserName() + ":" + currPlayer.getPassword();

                        String response = generateUDPResponse(newServerPort,"7:" + playerInfo);

                       // System.out.println(response + "R1");
                        if (response.equalsIgnoreCase("Successful")) {

                            playerList.remove(i);
                            playersTable.put(playerKey, playerList);

                            LOGGER.info("Player " + "Username=" + Username + " has been transferred to  - " + NewIPAddress);

                            result = currPlayer.getUserName() + " has been transferred to - " + NewIPAddress;
                        } else {

                            result = currPlayer.getUserName() + " cannot be transferred.";
                        }
                        break;
                    }
                }
            } else {
                LOGGER.info("Player not found - " + "Username=" + Username);
                result  = "User not found";
            }
        } finally {
            lock.unlock();
        }

        if (isLeader) {
                result = Constants.calculateEndResult(result, response1, response2, Constants.SERVER_PORT_AMERICA);
            System.out.println("L: " + result + " | R2: " + response1 + " | R3:  " + response2);
        }

        return result;
        // return "User not found";

    }

    @Override
    public String suspendAccount(String AdminUsername, String AdminPassword, String AdminIP, String UsernameToSuspend) {

        LOGGER.info("Received request - Suspend Player - " + "Username=" + UsernameToSuspend);

        String result = UsernameToSuspend + " not found";
        String response1 = "";
        String response2 = "";

        if (isLeader) {
            String action = "4:" + AdminUsername  + ":" + AdminPassword + ":" + AdminIP + ":" + UsernameToSuspend;
            response1 = generateUDPResponse(Constants.L_SERVER_PORT_AMERICA, action);
            response2 = generateUDPResponse(Constants.R2_SERVER_PORT_AMERICA, action);
        }
        char playerKey = UsernameToSuspend.charAt(0);

        try {
            // lock while performing operations
            lock.lock();
            if (playersTable.containsKey(playerKey)) {

                ArrayList<Player> playerList = playersTable.get(playerKey);

                for (int i = 0; i < playerList.size(); i++) {
                    Player currPlayer = playerList.get(i);
                    if (currPlayer.getUserName().equalsIgnoreCase(UsernameToSuspend)) {

                        playerList.remove(i);
                        playersTable.put(playerKey, playerList);

                        LOGGER.info("Player Suspended - " + "Username=" + UsernameToSuspend);
                        result =  currPlayer.getUserName() + " has been suspended. ";
                        break;
                    }
                }
            } else {

                LOGGER.info("Player not found - " + "Username=" + UsernameToSuspend);
                result = UsernameToSuspend + " not found";
            }
        } finally {
            lock.unlock();
        }

        if (isLeader) {
                result = Constants.calculateEndResult(result, response1, response2, Constants.SERVER_PORT_AMERICA);
            System.out.println("L: " + result + " | R2: " + response1 + " | R3:  " + response2);
        }

        return result;
        //return UsernameToSuspend + " not found";
    }

    /**
     * getPlayerStatusUDP.
     *
     * @param serverPort - port to which UDP request is sent
     * @return the UDP response
     */
    private String generateUDPResponse(int serverPort, String action) {

        LOGGER.info("Created UDP request - Get player status from port " + serverPort);
        String[] response = {"No response from " + serverPort};

        FIFOUDPMessage FIFOUDPMessage = new FIFOUDPMessage();

        //create a new thread for UDP request
        Thread UDPThread = new Thread(() ->
        {
            try {
                response[0] = FIFOUDPMessage.getUDPResponse(action, serverPort, Constants.SERVER_PORT_AMERICA);

            } catch (Exception e) {
                System.out.println("Exception at getPlayerStatus: " + e.getLocalizedMessage());
            }

        });

        //UDPThread.setName("Thread - UDP " + serverPort);
         UDPThread.start();

        try {
            UDPThread.join();
        } catch (Exception e) {
            System.out.println("At getPlayerStatus:" + e.getLocalizedMessage());
        }
        LOGGER.info("Received UDP response from " + serverPort + " - " + response[0]);
        return response[0];

    }
    /**
     * checkUserName - to check if username exists on other servers using UDP
     *
     * @param userName - username to check
     * @return  username status
     */
    private boolean checkUserName(String userName) {
        FIFOUDPMessage FIFOUDPMessage = new FIFOUDPMessage();

        String check_asia = FIFOUDPMessage.getUDPResponse("3:" + userName, Constants.SERVER_PORT_ASIA, Constants.SERVER_PORT_AMERICA);
        String check_europe = FIFOUDPMessage.getUDPResponse("3:" + userName, Constants.SERVER_PORT_EUROPE, Constants.SERVER_PORT_AMERICA);

        return !check_asia.equalsIgnoreCase("User not found") || !check_europe.equalsIgnoreCase("User not found");
    }

    private void addDummyData(boolean run) {
        if (run) {addDummyDataHelper(new Player("Test123", "Test123", 25, "test123", "test123", String.valueOf(Constants.SERVER_IP_AMERICA), false));}
        addDummyDataHelper(new Player("Test", "Test", 21, "Test_America1", "test123", String.valueOf(Constants.SERVER_IP_AMERICA), false));
        addDummyDataHelper(new Player("Alex", "Alex", 21, "Alex1212", "alex123", String.valueOf(Constants.SERVER_IP_AMERICA), true));
        addDummyDataHelper(new Player("Alex", "Alex", 21, "qwqwqw", "qwqwqw", String.valueOf(Constants.SERVER_IP_AMERICA), true));
    }

    private void addDummyDataHelper(Player player){

        char playerKey = player.getUserName().charAt(0);

        ArrayList<Player> playerList;

        try {
            lock.lock();

            if (playersTable.containsKey(playerKey)) {

                playerList = playersTable.get(playerKey);

                playerList.add(player);

            } else {
                playerList = new ArrayList<>();
                playerList.add(player);
                playersTable.put(playerKey, playerList);

            }
        } finally {
            lock.unlock();
        }

    }

}
