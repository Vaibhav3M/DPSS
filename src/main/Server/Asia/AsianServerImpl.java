package main.Server.Asia;

import DPSS_CORBA.GameServerPOA;
import main.Constants.Constants;
import main.Model.Player;
import main.SendUDP.SendReceiveUDPMessage;
import org.omg.CORBA.ORB;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class AsianServerImpl extends GameServerPOA {

    private ORB orb;
    private static final long serialVersionUID = 7526472295622776147L;

    //lock to enable synchronization
    private static Lock lock = new ReentrantLock(true);
    //to store player info
    private static Hashtable<Character, ArrayList<Player>> playersTable = new Hashtable<>();
    //to log activities in a log file
    private static Logger LOGGER;

    protected AsianServerImpl(Logger logger) {
        super();
        LOGGER = logger;
        addDummyData();
    }

    // set ORB value
    public void setORB(ORB orb_val) { orb = orb_val; }

    @Override
    public String createPlayerAccount(String FirstName, String LastName, float Age, String Username, String Password, String IPAddress) {

        boolean isFromServerIP = (Integer.parseInt(IPAddress) == Constants.SERVER_IP_ASIA);
        //create player object
        Player player = new Player(FirstName, LastName, Math.round(Age) , Username, Password, String.valueOf(Constants.SERVER_IP_ASIA), false);

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

        return "Successful";
    }

    @Override
    public String playerSignIn(String Username, String Password, String IPAddress) {

        LOGGER.info("Received   request - SignIn Player - " + "Username=" + Username);

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
                        return currPlayer.getUserName() + " has logged in.";
                    }
                }
            } else {

                LOGGER.info("Player not found - " + "Username=" + Username);
                return Username + " not found";
            }
        } finally {
            lock.unlock();
        }

        return Username + " not found";
    }

    @Override
    public String playerSignOut(String Username, String IPAddress) {

        boolean isFromServerIP = (Integer.parseInt(IPAddress) == Constants.SERVER_IP_ASIA);

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
                                return currPlayer.getUserName() + " is not signed in.";
                            }
                            currPlayer.setSignedIn(false);
                            playerList.remove(i);
                            playerList.add(currPlayer);
                            playersTable.put(playerKey, playerList);
                        }
                        LOGGER.info("Player SignedOut - " + "Username=" + Username);

                        return currPlayer.getUserName() + " has logged out.";
                    }
                }
            } else {
                LOGGER.info("Player not found - " + "Username=" + Username);
                return "User not found";
            }
        } finally {
            lock.unlock();
        }

        return "User not found";
    }

    @Override
    public String getPlayerStatus(String AdminUsername, String AdminPassword, String IPAddress, boolean checkOtherServers) {
        if (!AdminUsername.equalsIgnoreCase("Admin") || !AdminPassword.equalsIgnoreCase("Admin")) {
            return "Username or password incorrect.";
        }

        String response = "AS: ";
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

        String response_America = "";
        String response_Europe = "";

        //Send UDP requests to other servers
        if (checkOtherServers) {
            response_America = generateUDPResponse(Constants.SERVER_PORT_AMERICA,"playerstatus");
            response_Europe = generateUDPResponse(Constants.SERVER_PORT_EUROPE,"playerstatus");
        }

        //append the results
        response = response + onlineCount + " online, " + offlineCount + " offline. " + response_America + response_Europe;
        return response;

    }

    @Override
    public String transferAccount(String Username, String Password, String OldIPAddress, String NewIPAddress) {

        LOGGER.info("Received request - Transfer Player - " + "Username= " + Username + " OldIP: " + OldIPAddress + " NewIP: " +  NewIPAddress);

        if(OldIPAddress.equalsIgnoreCase(NewIPAddress)) return "New IP and Old IP must be different";
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
                        String playerInfo = currPlayer.getFirstName() + "," + currPlayer.getLastName() + "," + currPlayer.getAge() + "," + currPlayer.getUserName() + "," + currPlayer.getPassword();

                        String response = generateUDPResponse(newServerPort,"transferPlayer:" + playerInfo);

                        if (response.equalsIgnoreCase("Successful")) {

                            playerList.remove(i);
                            playersTable.put(playerKey, playerList);

                            LOGGER.info("Player "+ "Username=" + Username + " has been transferred to  - " + NewIPAddress);

                            return currPlayer.getUserName() + " has been transferred to - " + NewIPAddress;
                        }
                        else{

                            return currPlayer.getUserName() + " cannot be transferred.";
                        }
                    }
                }
            } else {
                LOGGER.info("Player not found - " + "Username=" + Username);
                return "User not found";
            }
        } finally {
            lock.unlock();
        }

        return "User not found";
    }

    @Override
    public String suspendAccount(String AdminUsername, String AdminPassword, String AdminIP, String UsernameToSuspend) {

        LOGGER.info("Received request - Suspend Player - " + "Username=" + UsernameToSuspend);

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
                        return currPlayer.getUserName() + " has been suspended. ";
                    }
                }
            } else {

                LOGGER.info("Player not found - " + "Username=" + UsernameToSuspend);
                return UsernameToSuspend + " not found";
            }
        } finally {
            lock.unlock();
        }

        return UsernameToSuspend + " not found";
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

        SendReceiveUDPMessage sendReceiveUDPMessage = new SendReceiveUDPMessage();

        //create a new thread for UDP request
        Thread UDPThread = new Thread(() ->
        {
            try {
                response[0] = sendReceiveUDPMessage.getUDPResponse(action, serverPort, Constants.SERVER_PORT_ASIA);

            } catch (Exception e) {
                System.out.println("Exception at getPlayerStatus: " + e.getLocalizedMessage());
            }

        });

        UDPThread.setName("Thread - UDP " + serverPort);
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
        SendReceiveUDPMessage sendReceiveUDPMessage = new SendReceiveUDPMessage();

        String check_american = sendReceiveUDPMessage.getUDPResponse("UserName=" + userName, Constants.SERVER_PORT_AMERICA, Constants.SERVER_PORT_ASIA);
        String check_europe = sendReceiveUDPMessage.getUDPResponse("UserName=" + userName, Constants.SERVER_PORT_EUROPE, Constants.SERVER_PORT_ASIA);

        return !check_american.equalsIgnoreCase("User not found") || !check_europe.equalsIgnoreCase("User not found");
    }

    private void addDummyData() {
        addDummyDataHelper(new Player("Test", "Test", 21, "Test_Asia", "test123", String.valueOf(Constants.SERVER_IP_ASIA), false));
        addDummyDataHelper(new Player("Yin", "Li", 21, "YinLi2", "yinli2", String.valueOf(Constants.SERVER_IP_ASIA), true));
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
