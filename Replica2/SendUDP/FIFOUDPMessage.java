package Replica2.SendUDP;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

/**
 * The Send udp request message.
 */
public class FIFOUDPMessage {

    private int maxattempts = 2;
    private int timeout = 1500;
    private boolean reponseStatus = true;


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public void setMaxTries(int maxAttempts) {
        this.maxattempts = maxAttempts;
    }

    /**
     * Gets udp response.
     *
     * @param actionMessage       the action message
     * @param client_PORT_Address the client port address
     * @param sender_PORT_Address the sender port address
     * @return the udp response
     */
    public String getUDPResponse(String actionMessage, int client_PORT_Address, int sender_PORT_Address) {

        DatagramSocket datagramSocket = null;
        String response = "No response from " + client_PORT_Address;

        try{
            int attempts = 0;
            datagramSocket = new DatagramSocket();

            byte[] message = (sender_PORT_Address+":"+actionMessage).getBytes();
            InetAddress hostAddress = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message,message.length,hostAddress,client_PORT_Address);
            datagramSocket.send(request);

            byte[] buffer = new byte[1000];

            DatagramPacket serverResponse = new DatagramPacket(buffer,buffer.length);
            do {

                try {
                    datagramSocket.receive(serverResponse);
                } catch (InterruptedIOException e) {
                    attempts += 1;
                }
            }while(reponseStatus  || (attempts < maxattempts));

            response = new String(serverResponse.getData(),0,serverResponse.getLength());

        }catch (SocketException e){
            System.out.println("Socket creation failed due to: " + e.getLocalizedMessage());
        }catch (UnknownHostException e){
            System.out.println("Exception at unknown" + e.getLocalizedMessage());
        }catch (IOException e){
            System.out.println("Exception at IO" +e.getLocalizedMessage());
        }
        finally {
            if(datagramSocket != null) datagramSocket.close();
        }

        return response;

    }

}
