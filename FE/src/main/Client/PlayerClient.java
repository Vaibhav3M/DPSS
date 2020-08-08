package main.Client;

import DPSS_CORBA.GameServer;
import DPSS_CORBA.GameServerHelper;
import main.Constants.Constants;
import main.Constants.Validations;
import main.Model.Player;
import main.Utilities.CustomLogger;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class PlayerClient {

    //game server objects
    static GameServer gameServerImpl;
    private static GameServer serverAsia;
    private static GameServer serverAmerica;
    private static GameServer serverEurope;


    private static BufferedReader reader = new BufferedReader((new InputStreamReader(System.in)));
    private static int client_IP_Address = 0;
    private static int server_port_number = 0;
    private static String client_server_name = "";
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static FileHandler fileHandler = null;

    //setup CORBA object and register
    private static boolean setupCORBA(String[] arguments){
        try {

            // create and initialize the ORB
            ORB orb = ORB.init(arguments, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // resolve the Object Reference in Naming
            serverAsia = GameServerHelper.narrow(ncRef.resolve_str(Constants.SERVER_NAME_ASIA));
            serverAmerica = GameServerHelper.narrow(ncRef.resolve_str(Constants.SERVER_NAME_AMERICA));
            serverEurope = GameServerHelper.narrow(ncRef.resolve_str(Constants.SERVER_NAME_EUROPE));


        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
            return false;
        }
        
        return true;
    }


    public static void main(String[] args) throws Exception{
        
        // setup CORBA
        if (!setupCORBA(args)) {
            System.out.println("Server setup failed, please restart session");
        }

        boolean exit = false;

        while (!exit) {

            int userinput = showMenu();

            switch (userinput) {

                case 1:
                    client_IP_Address = 0;
                    Player newPlayer = createPlayer();
                    String result1 = gameServerImpl.createPlayerAccount(newPlayer.getFirstName(), newPlayer.getLastName(), newPlayer.getAge(), newPlayer.getUserName(), newPlayer.getPassword(), newPlayer.getIPAddress());
                    setupLogging(newPlayer.getUserName());
                    LOGGER.info(newPlayer.getUserName() + " request to create new account on " + client_server_name);
                    LOGGER.info(result1);
                    if (fileHandler != null) fileHandler.close();
                    System.out.println("Message: " +result1);

                    break;

                case 2:
                    client_IP_Address = 0;
                    System.out.print("Please enter user name: ");
                    String userNameLogin = reader.readLine().trim();

                    System.out.print("Please enter password: ");
                    String password = reader.readLine().trim();

                    while (!Validations.validateIP(client_IP_Address)) {

                        System.out.print("Please enter IP starting (132, 93, 182): ");
                        client_IP_Address = getValidIntegerInput();
                    }
                    getServerFromIP(client_IP_Address);


                    System.out.println();

                    setupLogging(userNameLogin);
                    LOGGER.info(userNameLogin + " attempted to sign in on " + client_server_name);
                    String result2 = gameServerImpl.playerSignIn(userNameLogin, password,
                            String.valueOf(client_IP_Address));
                    LOGGER.info(result2);
                    System.out.println("Message: " +result2);
                    if (fileHandler != null) fileHandler.close();


                    break;

                case 3:
                    client_IP_Address = 0;
                    System.out.print("Please enter user name: ");
                    String userNameLogout = reader.readLine().trim();

                    while (!Validations.validateIP(client_IP_Address)) {

                        System.out.print("Please enter IP starting (132, 93, 182): ");
                        client_IP_Address = getValidIntegerInput();
                    }
                    getServerFromIP(client_IP_Address);

                    System.out.println();
                    setupLogging(userNameLogout);
                    LOGGER.info(userNameLogout + " attempted to sign out of " + client_server_name);


                    String result3 = gameServerImpl.playerSignOut(userNameLogout, String.valueOf(client_IP_Address));
                    LOGGER.info(result3);
                    if (fileHandler != null) fileHandler.close();
                    System.out.println("Message: " + result3);

                    break;

                case 4:

                    client_IP_Address = 0;
                    System.out.print("Please enter user name: ");
                    String userNameTransferAcc = reader.readLine().trim();

                    System.out.print("Please enter password: ");
                    String passwordTransferAcc = reader.readLine().trim();

                    int old_IP_Address = 0;
                    int new_IP_Address = 0;

                    while (!Validations.validateIP(old_IP_Address)) {

                        System.out.print("Please enter Old IP: ");
                        old_IP_Address = getValidIntegerInput();
                    }
                    getServerFromIP(old_IP_Address);

                    while (!Validations.validateIP(new_IP_Address)) {

                        System.out.print("Please enter New IP: ");
                        new_IP_Address = getValidIntegerInput();
                    }
                    //getServerFromIP(new_IP_Address);

                    System.out.println();
                    setupLogging(userNameTransferAcc);
                    LOGGER.info(userNameTransferAcc + " attempted to transfer account from Old IP: " + old_IP_Address + " to New IP: " + new_IP_Address);

                    String result4 = gameServerImpl.transferAccount(userNameTransferAcc, passwordTransferAcc, String.valueOf(old_IP_Address),String.valueOf(new_IP_Address));
                    LOGGER.info(result4);
                    if (fileHandler != null) fileHandler.close();
                    System.out.println("Message: " + result4);

                    break;

                case 5:
                    LOGGER.info("Exited the session.");
                    if (fileHandler != null) fileHandler.close();
                    System.out.println("Thank you for visiting our DPSS app");

                    exit = true;
                    break;

                default:
                    System.out.println("Oops..! Invalid input. Please select 1,2,3 or 4 to perform required action");

            }
        }
        
        
    }

    // Return basic menu.
    private static int showMenu() throws Exception {


        System.out.println("\n**** Welcome to DPSS Game ****\n");

        int userinput = 1;

        System.out.println("Please select an option (1-4)");
        System.out.println("1. Create new Player");
        System.out.println("2. SignIn");
        System.out.println("3. SignOut");
        System.out.println("4. Transfer account to new IP Address");
        System.out.println("5. Exit");

        System.out.print("Please select an Option : ");

        userinput = getValidIntegerInput();

        return userinput;

    }

    /**
     * createPlayer. - Validates and creates Player object
     *
     * @throws  Exception
     */
    private static Player createPlayer() throws Exception {

        // inputting first name
        System.out.print("Please enter first name: ");
        String firstName = reader.readLine().trim();

        // inputting last name
        System.out.print("Please enter last name: ");
        String lastName = reader.readLine().trim();

        // inputting age
        boolean ageInt = false;
        int age = 0;

        do {
            try {
                System.out.print("Please enter your age: ");
                age = getValidIntegerInput();
                ageInt = true;
            } catch (Exception e) {
                System.out.println("Invalid age");
            }
        } while (!ageInt & Validations.validateAge(age));

        // inputting username
        System.out.print("Please enter a unique username: ");
        String userName = reader.readLine();
        while (!Validations.validateUserName(userName)) {
            System.out.println("Error: Username must be between 5 to 15 characters");
            System.out.print("Please enter a unique username: ");
            userName = reader.readLine().trim();
        }

        // inputting password
        System.out.print("Please enter password: ");
        String password = reader.readLine().trim();

        while (!Validations.validatePassword(password)) {
            System.out.println("Error: Password must be minimum 6 characters");
            System.out.print("Please enter password: ");
            password = reader.readLine().trim();
        }


        while (!Validations.validateIP(client_IP_Address)) {

            System.out.print("Please enter IP starting (132, 93, 182): ");
            client_IP_Address = getValidIntegerInput();
        }
        getServerFromIP(client_IP_Address);


        System.out.println();

        return new Player(firstName, lastName, age, userName, password, String.valueOf(client_IP_Address), false);

    }

    /**
     * getServerFromIP. - This method takes IP by user and gets   registry
     *
     * @param client_IP_Address IP address entered by user
     */
    private static void getServerFromIP(int client_IP_Address){


        switch (client_IP_Address) {

            case 132:
                client_server_name = Constants.SERVER_NAME_AMERICA;
                server_port_number = Constants.SERVER_PORT_AMERICA;
                gameServerImpl = serverAmerica;
                break;

            case 93:
                client_server_name = Constants.SERVER_NAME_EUROPE;
                server_port_number = Constants.SERVER_PORT_EUROPE;
                gameServerImpl = serverEurope;
                break;

            case 182:
                client_server_name = Constants.SERVER_NAME_ASIA;
                server_port_number = Constants.SERVER_PORT_ASIA;
                gameServerImpl = serverAsia;
                break;
            default:
                System.out.println("Error: Invalid server IP");


        }
    }

    /**
     * getValidIntegerInput. - Takes input from console and validates proper integer
     *
     */
    private static int getValidIntegerInput() {

        int value = 0;
        boolean inputValid = false;
        do {
            try {
                String input = reader.readLine().trim();
                if(input.contains(".")){
                    input = input.split("\\.")[0];
                }
                value = Integer.valueOf(input);
                inputValid = true;

            } catch (Exception e) {
                System.out.println("Error: This field requires a number value. Please try again");
            }
        } while (!inputValid);

        return value;
    }

    /**
     * setupLogging. - Setup logger for the class
     * @param name - to create log file for player username
     */
    private static void setupLogging(String name) throws IOException {
        File files = new File(Constants.PLAYER_LOG_DIRECTORY);
        if (!files.exists())
            files.mkdirs();
        files = new File(Constants.PLAYER_LOG_DIRECTORY+"Player_"+ name+ ".log");
        if(!files.exists())
            files.createNewFile();
        fileHandler = CustomLogger.setup(files.getAbsolutePath());
    }

}
