package main.Server.Europe;

import DPSS_CORBA.GameServer;
import DPSS_CORBA.GameServerHelper;
import main.Constants.Constants;
import main.Utilities.CustomLogger;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class EuropeanServer {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // to manage log files
    static FileHandler fileHandler = null;

    /**
     *Recieve - Setup UDP server to recieve requests.
     *
     * @param serverImpl the server
     */
    public static void recieve(EuropeanServerImpl serverImpl) {

        String responseString = "";
        DatagramSocket dataSocket = null;

        try {

            dataSocket = new DatagramSocket(Constants.SERVER_PORT_EUROPE);
            byte[] buffer = new byte[1000];
            LOGGER.info( "Server started..!!!");

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                dataSocket.receive(request);
                String requestMessage = new String(request.getData(),0,request.getLength());

                LOGGER.info("Received UDP request message: " + requestMessage);

                String request_IP = requestMessage.split(":")[0];
                requestMessage = requestMessage.split(":")[1];

                if (requestMessage.split("=")[0].equalsIgnoreCase("username")) {
                    responseString = serverImpl.playerSignOut(requestMessage.split("=")[1],request_IP);
                }else if (requestMessage.equalsIgnoreCase("transferPlayer")){

                    String playerString = new String(request.getData(),0,request.getLength()).split(":")[2];
                    String[] playerArray = playerString.split(",");

                    responseString = serverImpl.createPlayerAccount(playerArray[0],playerArray[1],Integer.parseInt(playerArray[2]),playerArray[3],playerArray[4],String.valueOf(Constants.SERVER_IP_AMERICA));

                } else {
                    responseString = serverImpl.getPlayerStatus("Admin", "Admin", String.valueOf(request.getPort()), false);
                }

                LOGGER.info("Sent UDP response message: " + responseString);
                DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());

                dataSocket.send(reply);
            }

        } catch (SocketException e) {
            LOGGER.info("Exception at socket" +e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.info("Exception at IO" +e.getLocalizedMessage());
        } finally {
            if (dataSocket != null) dataSocket.close();
            if (fileHandler != null) fileHandler.close();
        }

    }

    public static void main(String args[]) {

        EuropeanServerImpl serverImplementation = new EuropeanServerImpl(LOGGER);
        Thread server_europe = new Thread(()->
        {
            try {
                //setup logger
                setupLogging();
                //UDP setup
                recieve(serverImplementation);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" +e.getLocalizedMessage());
            }
        });

        server_europe.setName("thread_Europe_server");
        server_europe.start();

        // create and initialize the ORB
        ORB orb = ORB.init(args, null);
        try {

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();
            // create servant and register it with the ORB
            serverImplementation.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(serverImplementation);

            GameServer href = GameServerHelper.narrow(ref);
            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt which is part of the Interoperable Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent path[] = ncRef.to_name(Constants.SERVER_NAME_EUROPE);
            ncRef.rebind(path, href);
            System.out.println("EuropeanServer ready and waiting ...");
            // wait for invocations from clients
            orb.run();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getLocalizedMessage());
         //   e.printStackTrace(System.out);
            System.out.println("Some issue here ");
        }
        orb.shutdown(false);
        System.out.println("EuropeanServer Exiting ...");
    }

    /**
     * setupLogging. - Setup logger for the class
     */
    private static void setupLogging() throws IOException {
        File files = new File(Constants.SERVER_LOG_DIRECTORY);
        if (!files.exists())
            files.mkdirs();
        files = new File(Constants.SERVER_LOG_DIRECTORY+"EUROPE_Server.log");
        if(!files.exists())
            files.createNewFile();
        fileHandler = CustomLogger.setup(files.getAbsolutePath());
    }
}

