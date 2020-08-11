package Replica2.main.Constants;

public class Constants {

    public static boolean isLeader = false;

    /* Game server ports */
    public static final int SERVER_PORT_AMERICA = 3001;
    public static final int SERVER_PORT_EUROPE = 3002;
    public static final int SERVER_PORT_ASIA = 3003;

    public static final int L_SERVER_PORT_AMERICA = 2421;
    public static final int L_SERVER_PORT_EUROPE = 5892;
    public static final int L_SERVER_PORT_ASIA = 8091;

    public static final int R1_SERVER_PORT_AMERICA = 2001;
    public static final int R1_SERVER_PORT_EUROPE = 2002;
    public static final int R1_SERVER_PORT_ASIA = 2003;

    /* Game server IPs */
    public static final int SERVER_IP_AMERICA = 132;
    public static final int SERVER_IP_EUROPE = 93;
    public static final int SERVER_IP_ASIA = 182;

    /* Game server names */
    public static final String SERVER_NAME_AMERICA = "R2_AmericaGameServer";
    public static final String SERVER_NAME_EUROPE = "R2_EuropeGameServer";
    public static final String SERVER_NAME_ASIA = "R2_AsiaGameServer";

    /* Log storage locations */
    public static final String SERVER_LOG_DIRECTORY = "./src/Logs/Server_2/";
    public static final String PLAYER_LOG_DIRECTORY = "./src/Logs/Player_2/";
    public static final String ADMIN_LOG_DIRECTORY = "./src/Logs/Admin_2/";

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

    public static String calculateEndResult(String result, String response1, String response2) {

        String response = "";
        // 3. Compare the results
        String RMRequestData = "";

        // R1 == R2 == R3 -> T|T|T
        if(result.equals(response1) && result.equals(response2)){

            // all are same
            // send this to RM => "T|T|T"
            RMRequestData = "T|T|T";
            // Send outputR1/R2/R3 to Front-End
            response      =  result;
        }
        // R1 != R2 == R3 -> F|T|T
        else if(!result.equals(response1) && result.equals(response2)) {
            // Leader(R1) is wrong
            // send this to RM => "F|T|T"
            RMRequestData = "F|T|T";
            // Send outputR2/R3 to Front-End
            response      =  response1;
        }
        // R1 == R2 != R3 -> T|T|F
        else if(result.equals(response1) && !result.equals(response2)){

            // R1 == R2
            // R3 is wrong
            // send this to RM => "T|T|F"
            RMRequestData = "T|T|F";
            // Send outputR1/R2 to Front-End
            response      =  response1;

        }
        // R1 != R2 != R3 AND (R1 == R3) -> T|F|T
        else if(result.equals(response2) && !result.equals(response1) && !response2.equals(response1) ){
            // R2 is wrong
            // leader, R1 right (Send this to the client)
            // send this to RM => "T|F|T"
            RMRequestData = "F|T|T";
            // Send outputR1/R3 to Front-End
            response      =  response2;
        }

        // 4. send results to RM
        // SendResultsToRM(RMRequestData);

        return response;
    }


}
