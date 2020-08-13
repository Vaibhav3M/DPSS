package Replica2.Server.Asia;

import GameServer_CORBA.GameServer;
import GameServer_CORBA.GameServerHelper;
import Replica2.Constants.Constants;
import Replica2.Utilities.CustomLogger;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class R2_AsianServer {

    private static ConcurrentLinkedQueue<String> requestQueue = new ConcurrentLinkedQueue<>();

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // to manage log files
    static FileHandler fileHandler = null;
    private static DatagramSocket socket;

    /**
     *Recieve - Setup UDP server to recieve requests.
     *
     * @param serverImpl the server
     */
    public static void recieve(R2_AsianServerImpl serverImpl) {

        String responseString = "";
        DatagramSocket dataSocket = null;

        try {

            dataSocket = new DatagramSocket(Constants.SERVER_PORT_ASIA);
            byte[] buffer = new byte[1000];
            LOGGER.info( "Server started..!!!");

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                dataSocket.receive(request);
                String requestMessage = new String(request.getData(),0,request.getLength());

                LOGGER.info("Received UDP request message: " + requestMessage);

                              if(!requestQueue.isEmpty()){
                    System.out.println("Request from Queue");
                    socket.receive(request);
                }
                String[] data = requestMessage.split(":");
                String request_IP = data[0];

                switch (data[1].trim()){

                    case "1" : responseString = serverImpl.createPlayerAccount(data[2],data[3],Integer.parseInt(data[4]),data[5],data[6],String.valueOf(Constants.SERVER_IP_ASIA));
                        break;
                    case "2" : responseString = serverImpl.playerSignIn(data[2],data[3],data[4]);
                        break;
                    case "3" : responseString = serverImpl.playerSignOut(data[2],request_IP);
                        break;
                    case "4" : responseString = serverImpl.suspendAccount(data[2],data[3],data[4],data[5]);
                        break;
                    case "5" : responseString = serverImpl.transferAccount(data[2],data[3],data[4],data[5]);
                        break;
                    case "6" : responseString = serverImpl.getPlayerStatus("Admin", "Admin", String.valueOf(request.getPort()), false);
                        break;
                    case "7" : responseString = serverImpl.createPlayerAccount(data[2],data[3],Integer.parseInt(data[4]),data[5],data[6],"1212"); //here IP address to to just check server
                        break;

                }

//                String request_IP = requestMessage.split(":")[0];
//                requestMessage = requestMessage.split(":")[1];
//
//                if (requestMessage.split("=")[0].equalsIgnoreCase("username")) {
//                    responseString = serverImpl.playerSignOut(requestMessage.split("=")[1],request_IP);
//                }else if (requestMessage.equalsIgnoreCase("transferPlayer")){
//                    System.out.println(requestMessage);
//                    String playerString = new String(request.getData(),0,request.getLength()).split(":")[2];
//                    String[] playerArray = playerString.split(",");
//
//                    responseString = serverImpl.createPlayerAccount(playerArray[0],playerArray[1],Integer.parseInt(playerArray[2]),playerArray[3],playerArray[4],String.valueOf(Constants.SERVER_IP_AMERICA));
//                } else {
//                    responseString = serverImpl.getPlayerStatus("Admin", "Admin", String.valueOf(request.getPort()), false);
//                }

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

        R2_AsianServerImpl serverImplementation = new R2_AsianServerImpl(LOGGER);
        Thread server_asia = new Thread(()->
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

        server_asia.setName("thread_Asia_server");
        server_asia.start();

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
            NameComponent path[] = ncRef.to_name(Constants.SERVER_NAME_ASIA);
            ncRef.rebind(path, href);
            System.out.println("R3_AsianServer launched at port : - " + Constants.SERVER_PORT_ASIA);

            // wait for invocations from clients
            orb.run();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
        orb.shutdown(false);
        System.out.println("AsianServer Exiting ...");
    }

    /**
     * setupLogging. - Setup logger for the class
     */
    private static void setupLogging() throws IOException {
        File files = new File(Constants.SERVER_LOG_DIRECTORY);
        if (!files.exists())
            files.mkdirs();
        files = new File(Constants.SERVER_LOG_DIRECTORY+"ASIA_Server.log");
        if(!files.exists())
            files.createNewFile();
        fileHandler = CustomLogger.setup(files.getAbsolutePath());
    }

    private static boolean testRuns(){
        boolean result = true;
        //test
        try {
            requestQueue.add("");
            requestQueue.remove("");
        }catch (Exception e){
            result  = false;
        }
        return result;
    }
}
