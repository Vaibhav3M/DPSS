package ReplicaManager;

import ReplicaL.Constants.Constants;
import ReplicaL.Server.America.AmericanServer;
import ReplicaL.Server.Asia.AsianServer;
import ReplicaL.Server.Europe.EuropeanServer;
import Replica1.Server.America.R1_AmericanServer;
import Replica1.Server.Asia.R1_AsianServer;
import Replica1.Server.Europe.R1_EuropeanServer;
import Replica2.Server.America.R2_AmericanServer;
import Replica2.Server.Asia.R2_AsianServer;
import Replica2.Server.Europe.R2_EuropeanServer;
import Replica_Faulty.Server.America.F_AmericanServer;
import Replica_Faulty.Server.Asia.F_AsianServer;
import Replica_Faulty.Server.Europe.F_EuropeanServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ReplicaManager {

    private static boolean testByzantine = true;

    //1 is R1, 2 is R2, 3 is R3
    private static int leader_id = 1;

    private static final int REPLICA_SERVER_PORT = 4000;

    static int errorCount_R1 = 0;
    static int errorCount_R2 = 0;
    static int errorCount_R3 = 0;

    private static Thread[] R1 = new Thread[3];
    private static Thread[] R2 = new Thread[3];
    private static Thread[] R3 = new Thread[3];

    private static boolean run = false;



    public static void recieve(String[] args) {

        String responseString = "";
        DatagramSocket dataSocket = null;

        try {

            dataSocket = new DatagramSocket(REPLICA_SERVER_PORT);
            byte[] buffer = new byte[1000];
            //LOGGER.info("Server started..!!!");
            String launchMessage = "RM launched at : - " + REPLICA_SERVER_PORT;

            if(testByzantine){launchMessage = launchMessage + " with Byzantine error";}

            System.out.println(launchMessage);
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                dataSocket.receive(request);
                String requestMessage = new String(request.getData(), 0, request.getLength());

                //  LOGGER.info("Received UDP request message: " + requestMessage);

                String[] data = requestMessage.split(":");
                String request_IP = data[0];

                switch (data[1].trim()) {

                    case "1":
                        responseString = wrongAnswerNotification(data[2], args);
                        break;
                    default:
                        responseString = "Error occured";
                        break;

                }

                //LOGGER.info("Sent UDP response message: " + responseString);
                DatagramPacket reply = new DatagramPacket(responseString.getBytes(), responseString.length(), request.getAddress(), request.getPort());

                dataSocket.send(reply);
            }

        } catch (SocketException e) {
            // LOGGER.info("Exception at socket" + e.getLocalizedMessage());
        } catch (IOException e) {
            // LOGGER.info("Exception at IO" + e.getLocalizedMessage());
        } finally {
            if (dataSocket != null) dataSocket.close();
            //if (fileHandler != null) fileHandler.close();
        }

    }

    public static void main(String[] args) {

        Thread RM_thread = new Thread(() ->
        {
            try {

                //UDP setup
                recieve(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        RM_thread.setName("thread_RM");
        RM_thread.start();

        RMsetup(1, args);


    }

    private static void RMsetup(int leader, String[] args) {
        errorCount_R1 = 0;
        errorCount_R2 = 0;
        errorCount_R3 = 0;

        leader_id = leader;

        switch (leader){
            case 1:
                Constants.isLeader = true;
                break;
            case 2:
                Replica1.Constants.Constants.isLeader = true;
                break;
            case 3:
                Replica2.Constants.Constants.isLeader = true;
                break;
        }

        if(testByzantine) {
            R1 = startReplica1(args);
            R2 = startReplicaFaulty(args);
            R3 = startReplica3(args);
        }else{
            R1 = startReplica1(args);
            R2 = startReplica2(args);
            R3 = startReplica3(args);
        }

        ShutDownTask shutDownTask = new ShutDownTask();
        Runtime.getRuntime().addShutdownHook(shutDownTask);

    }

    private static String wrongAnswerNotification(String datum, String args[]) {

        String response = "All three servers matched";
        String[] results = datum.trim().split("&");

        for (int i = 0; i < 3; i++) {

            if (results[i].equals("T")) {
                // Skip it
                if (i == 0) {
                    errorCount_R1 = 0;
                }
                if (i == 1) {
                    errorCount_R2 = 0;
                }
                if (i == 2) {
                    errorCount_R3 = 0;
                }
            } else if (results[i].equals("F")) {
                // Increment by 1
                if (i == 0) {
                    response = "Replica1 didn't match";
                    errorCount_R1++;
                }
                if (i == 1) {
                    response = "Replica2 didn't match";

                    errorCount_R2++;
                }
                if (i == 2) {
                    response = "Replica3 didn't match";
                    errorCount_R3++;
                }
            }

        }

        // Check counter values
        if (errorCount_R1 >= 3) {
            errorCount_R1 = 0;
            interruptThreads(R1);
            // Start new Replica 1 as leader;
            startReplica1(args);
            response = "Replica1 has been restarted";
        }

        if (errorCount_R2 >= 3) {
            errorCount_R2 = 0;
            interruptThreads(R2);
            // Start new  Replica 2;
            run = true;
            startReplicaFaulty(args);
            response = "Replica2 has been restarted";

        }

        if (errorCount_R3 >= 3) {
            errorCount_R3 = 0;
            interruptThreads(R3);
            // Start new Replica 3;
            startReplica3(args);
            response = "Replica3 has been restarted";

        }

        return response;
    }


    private static void interruptThreads(Thread[] r1) {
        for (Thread t : r1) {
            t.interrupt();
        }
    }


    private static Thread[] startReplica1(String[] args) {

        Thread server_america = new Thread(() ->
        {
            try {
                AmericanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_america.setName("thread_America_server");
        server_america.start();

        Thread server_asia = new Thread(() ->
        {
            try {
                AsianServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_asia.setName("thread_Asia_server");
        server_asia.start();

        Thread server_europe = new Thread(() ->
        {
            try {
                EuropeanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_europe.setName("thread_Europe_server");
        server_europe.start();

        return new Thread[]{server_america, server_asia, server_europe};

    }

    private static Thread[] startReplica2(String[] args) {


        Thread server_america = new Thread(() ->
        {
            try {
                R1_AmericanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

       // server_america.setName("thread_R1_America_server");
        server_america.start();

        Thread server_asia = new Thread(() ->
        {
            try {
                R1_AsianServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

      //  server_asia.setName("thread_R1_Asia_server");
        server_asia.start();

        Thread server_europe = new Thread(() ->
        {
            try {
                R1_EuropeanServer.main(args);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

       // server_europe.setName("thread_R1_Europe_server");
        server_europe.start();

        return new Thread[]{server_america, server_asia, server_europe};

    }

    private static Thread[] startReplica3(String[] args) {


        Thread server_america = new Thread(() ->
        {
            try {
                R2_AmericanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_america.setName("thread_R2_America_server");
        server_america.start();

        Thread server_asia = new Thread(() ->
        {
            try {
                R2_AsianServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_asia.setName("thread_R2_Asia_server");
        server_asia.start();

        Thread server_europe = new Thread(() ->
        {
            try {
                R2_EuropeanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

        server_europe.setName("thread_R2_Europe_server");
        server_europe.start();

        return new Thread[]{server_america, server_asia, server_europe};
    }

    private static Thread[] startReplicaFaulty(String[] args) {


        Thread server_america = new Thread(() ->
        {
            try {
                if(run) {F_AmericanServer.run = true; }
                F_AmericanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

      //  server_america.setName("thread_R2_America_server");
        server_america.start();

        Thread server_asia = new Thread(() ->
        {
            try {
                F_AsianServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

      //  server_asia.setName("thread_R2_Asia_server");
        server_asia.start();

        Thread server_europe = new Thread(() ->
        {
            try {
                F_EuropeanServer.main(args);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception at main" + e.getLocalizedMessage());
            }
        });

      //  server_europe.setName("thread_R2_Europe_server");
        server_europe.start();

        return new Thread[]{server_america, server_asia, server_europe};
    }

    private static class ShutDownTask extends Thread {

        @Override
        public void run() {
            interruptThreads(R1);
            interruptThreads(R2);
            interruptThreads(R3);
            System.out.println("All the Servers have been stopped");

        }
    }
}
