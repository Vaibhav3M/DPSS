package Replica1.main.Constants;

import ReplicaL.SendUDP.FIFOUDPMessage;

public class Constants {

    public static boolean isLeader = false;

    /* Game server ports */
    public static final int SERVER_PORT_AMERICA = 2001;
    public static final int SERVER_PORT_EUROPE = 2002;
    public static final int SERVER_PORT_ASIA = 2003;

    public static final int L_SERVER_PORT_AMERICA = 2421;
    public static final int L_SERVER_PORT_EUROPE = 5892;
    public static final int L_SERVER_PORT_ASIA = 8091;

    public static final int R2_SERVER_PORT_AMERICA = 3001;
    public static final int R2_SERVER_PORT_EUROPE = 3002;
    public static final int R2_SERVER_PORT_ASIA = 3003;

    /* Game server IPs */
    public static final int SERVER_IP_AMERICA = 132;
    public static final int SERVER_IP_EUROPE = 93;
    public static final int SERVER_IP_ASIA = 182;

    /* Game server names */
    public static final String SERVER_NAME_AMERICA = "R1_AmericaGameServer";
    public static final String SERVER_NAME_EUROPE = "R1_EuropeGameServer";
    public static final String SERVER_NAME_ASIA = "R1_AsiaGameServer";

    /* Log storage locations */
    public static final String SERVER_LOG_DIRECTORY = "./Logs/Server_1/";
    public static final String PLAYER_LOG_DIRECTORY = "./Logs/Player_1/";
    public static final String ADMIN_LOG_DIRECTORY = "./Logs/Admin_1/";

    public static int getServerPortFromIP(int ip){

        switch(ip){

            case SERVER_IP_AMERICA:
                return SERVER_PORT_AMERICA;

            case SERVER_IP_ASIA:
                return SERVER_PORT_ASIA;

            case SERVER_IP_EUROPE:
                return SERVER_PORT_EUROPE;

        }

        return 0;
    }

    public static String calculateEndResult(String result, String response1, String response2, int serverPort) {

        String response = result;
        // Compare the results
        String RMRequestData = "T&T&T";

        // R1 == R2 == R3
        if(result.equals(response1) && result.equals(response2)){

            // all are same
            // send this to RM
            RMRequestData = "T&T&T";

            response = result;
        }
        // R1 != R2 == R3 -> F|T|T
        else if(!result.equals(response1) && result.equals(response2)) {
            // Leader(R1) is wrong

            // send this to RM
            RMRequestData = "F&T&T";

            response = response1;
        }
        // R1 == R2 != R3 -> T|T|F
        else if(result.equals(response1) && !result.equals(response2)){

            // R1 == R2
            // R3 is wrong
            // send this to RM
            RMRequestData = "T&T&F";

            response = response1;

        }
        // R1 != R2 != R3 AND (R1 == R3) -> T|F|T
        else if(result.equals(response2) && !result.equals(response1) && !response2.equals(response1) ){
            // R2 is wrong
            // leader, R1 right
            // send this to RM
            RMRequestData = "T&F&F";
            // Send outputR1/R3 to Front-End
            response =  response2;
        }

        //send results to RM
        sendResultToRM(serverPort,RMRequestData);

        return response;
    }

    private static String sendResultToRM(int requestServerPort, String rmRequestData) {


        //LOGGER.info("Created UDP request - Get player status from port " + serverPort);
        String[] response = {"No response from RM"};

        FIFOUDPMessage FIFOUDPMessage = new FIFOUDPMessage();

        System.out.println(rmRequestData);
        //create a new thread for UDP request
        Thread UDPThread = new Thread(() ->
        {
            try {
                response[0] = FIFOUDPMessage.getUDPResponse("1:"+rmRequestData, 4000, requestServerPort);
                System.out.println(response[0]);

            } catch (Exception e) {
                System.out.println("Exception at getPlayerStatus: " + e.getLocalizedMessage());
            }

        });

        UDPThread.setName("Thread - UDP " + requestServerPort);
        UDPThread.start();

        try {
            UDPThread.join();
        } catch (Exception e) {
            System.out.println("At getPlayerStatus:" + e.getLocalizedMessage());
        }
        //LOGGER.info("Received UDP response from " + serverPort + " - " + response[0]);
        return response[0];

    }


}
