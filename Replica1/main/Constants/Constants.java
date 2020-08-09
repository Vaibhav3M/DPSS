package Replica1.main.Constants;

public class Constants {

    /* Game server ports */
    public static final int SERVER_PORT_AMERICA = 2001;
    public static final int SERVER_PORT_EUROPE = 2002;
    public static final int SERVER_PORT_ASIA = 2003;

    /* Game server IPs */
    public static final int SERVER_IP_AMERICA = 132;
    public static final int SERVER_IP_EUROPE = 93;
    public static final int SERVER_IP_ASIA = 182;

    /* Game server names */
    public static final String SERVER_NAME_AMERICA = "R1_AmericaGameServer";
    public static final String SERVER_NAME_EUROPE = "R1_EuropeGameServer";
    public static final String SERVER_NAME_ASIA = "R1_AsiaGameServer";

    /* Log storage locations */
    public static final String SERVER_LOG_DIRECTORY = "./src/Logs/Server/";
    public static final String PLAYER_LOG_DIRECTORY = "./src/Logs/Player/";
    public static final String ADMIN_LOG_DIRECTORY = "./src/Logs/Admin/";

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

}
