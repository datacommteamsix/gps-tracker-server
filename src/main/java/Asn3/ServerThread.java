package Asn3;

import com.google.firebase.database.DatabaseReference;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerThread extends Thread {
    private byte[] ServerString = new byte[512];
    private Socket clientSocket;
    private DatabaseReference databaseReference;

    /**
     * The Server Constructor
     *
     * @throws IOException
     */
    public ServerThread(Socket clientSocket, DatabaseReference databaseReference) throws IOException {
        this.clientSocket      = clientSocket;
        this.databaseReference = databaseReference;
        clientSocket.setSoTimeout(20000); // set a 20 second timeout
    }

    /**
     * The runnable method for the thread
     */
    public void run() {
            try {
                System.out.println("Sending dummy data to Firebase");
                DatabaseReference time = databaseReference.child("" + (System.currentTimeMillis() / 1000L));
                //Store the ip as the id for the client in the database
                time.setValueAsync(new GPSData());


                // This will receive the responses from the client
                //DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                // Print the number of bytes read
                //System.out.println("Bytes read from the stream: " + in.read(ServerString));
                // This will send data out to the client
                //DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                // write the data to the output stream
                //out.writeUTF(ServerString + clientSocket.getLocalSocketAddress());

                //Close the socket
                System.out.println("Closing socket");
                clientSocket.close();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}

/**
 * This is the "JSON" object we use to send data to Firebase
 *
 * The java identifier here acts as the key, while the rvalue is the value
 *
 * If you want to create a child node without a value, use getReference and pass
 * in a new path.
 */
class GPSData {
    public double latitude = 2.234;
    public double longitude = 3.345;

    public GPSData() {

    }
}