package ReplicaManager.src.main;

import FE.src.main.Server.America.AmericanServer;
import FE.src.main.Server.Asia.AsianServer;
import FE.src.main.Server.Europe.EuropeanServer;
import Replica1.main.Server.America.R1_AmericanServer;
import Replica1.main.Server.Asia.R1_AsianServer;
import Replica1.main.Server.Europe.R1_EuropeanServer;
import Replica2.main.Server.America.R2_AmericanServer;
import Replica2.main.Server.Asia.R2_AsianServer;
import Replica2.main.Server.Europe.R2_EuropeanServer;

public class ReplicaManager {

    int errorCount_R1 = 0;
    int errorCount_R2 = 0;
    int errorCount_R3 = 0;

    private static Thread[] R1 = new Thread[3];
    private static Thread[] R2 = new Thread[3];
    private static Thread[] R3 = new Thread[3];

    //1 is R1, 2 is R2, 3 is R3
    private static int leader_id = 1;

    public static void main(String[] args) {

        R1 = startReplica1(args);
        R2 = startReplica2(args);
        R3 = startReplica3(args);

        ShutDownTask shutDownTask = new ShutDownTask();
        Runtime.getRuntime().addShutdownHook(shutDownTask);

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

        server_america.setName("thread_R1_America_server");
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

        server_asia.setName("thread_R1_Asia_server");
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

        server_europe.setName("thread_R1_Europe_server");
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

    private static class ShutDownTask extends Thread {

        @Override
        public void run() {
            interruptThreads(R1);
            interruptThreads(R2);
            interruptThreads(R3);
            System.out.println("Servers stopped");

        }
    }
}
