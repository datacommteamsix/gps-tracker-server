package Asn3;

import com.google.firebase.database.DatabaseReference;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the servant thread that is spawned by the
 * server to handle each client that connects
 *
 * @author Angus Lam
 * @version 4-3-2018
 */
public class ServerThread extends Thread {
    /**
     * The client socket
     */
    private Socket clientSocket;
    /**
     * A reference to some part of the Firebase database
     */
    private DatabaseReference databaseReference;

    /**
     * The index of the device id
     */
    private static final int HASHED_DEVICE_ID_INDEX = 0;
    /**
     * The index of the time stamp
     */
    private static final int TIMESTAMP_INDEX = 1;
    /**
     * The index of the latitude
     */
    private static final int LATITUDE_INDEX = 2;
    /**
     * The index of the latitude
     */
    private static final int LONGITUDE_INDEX = 3;
    /**
     * The timeout period for the tcp connection in milliseconds
     */
    private static final int TIMEOUT = 20000;
    /**
     * The packet size for our gps data
     */
    private static final int GPS_PACKET_SIZE = 59;

    /**
     * This creates a thread using a client socket
     * and a reference to the firebase database
     *
     * @param clientSocket
     *          The client socket
     * @param databaseReference
     *          The firebase database reference
     * @throws IOException
     */
    public ServerThread(Socket clientSocket, DatabaseReference databaseReference) throws IOException {
        this.clientSocket      = clientSocket;
        this.databaseReference = databaseReference;
        clientSocket.setSoTimeout(TIMEOUT); // set a 20 second timeout
    }

    /**
     * The runnable method for the thread
     */
    public void run() {
            try {
                // This will receive the responses from the client
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());

                while(true) {
                    try {
                        if(in.available() > 0) {
                            // Count the number of available bytes in the stream
                            byte[] buffer = new byte[GPS_PACKET_SIZE];
                            System.out.println("Available bytes to read from stream : " + in.available() + " | buffer length: " + buffer.length);
                            // Create a buffer the size of the available bytes in stream
                            in.readFully(buffer);

                            List<byte[]> tokens = new ArrayList<byte[]>();
                            final byte delimiter = ' ';
                            int lastIndexOfDelimiter = -1;

                            //Break up the packet into tokens
                            for (int i = 0; i < buffer.length; i++) {
                                // If you hit a delimiter...
                                if (buffer[i] == delimiter) {
                                    int length = i - lastIndexOfDelimiter - 1;
                                    byte[] token = new byte[length];

                                    //Copy the token to array
                                    System.arraycopy(buffer, lastIndexOfDelimiter + 1, token, 0, length);
                                    //Add to the list
                                    tokens.add(token);

                                    //Set the index
                                    lastIndexOfDelimiter = i;
                                }

                                if (i == buffer.length - 1) {
                                    int length = buffer.length - lastIndexOfDelimiter - 1;
                                    byte[] token = new byte[length];

                                    //Copy the token to array
                                    System.arraycopy(buffer, lastIndexOfDelimiter + 1, token, 0, length);
                                    //Add to the list
                                    tokens.add(token);
                                }
                            }

                            ByteBuffer wrapped;

                            //Parse and print the content of the tokens
                            System.out.print("The parsed packet: ");

                            String hashedDeviceID = new String(tokens.get(HASHED_DEVICE_ID_INDEX));
                            System.out.print(hashedDeviceID);
                            System.out.print(" ");
                            wrapped = ByteBuffer.wrap(tokens.get(TIMESTAMP_INDEX)); // big-endian by default
                            long timestamp = wrapped.getLong();
                            System.out.print(timestamp + " ");

                            wrapped = ByteBuffer.wrap(tokens.get(LATITUDE_INDEX)); // big-endian by default
                            double latitude = wrapped.getDouble();
                            System.out.print(latitude + " ");

                            wrapped = ByteBuffer.wrap(tokens.get(LONGITUDE_INDEX)); // big-endian by default
                            double longitude = wrapped.getDouble();
                            System.out.print(longitude + " \n");

                            //Send the data to firebase
                            System.out.println("Sending parsed data to Firebase");
                            DatabaseReference device = databaseReference.child(hashedDeviceID);
                            DatabaseReference time = device.child(Long.toString(timestamp));
                            System.out.println(time.toString());
                            time.setValueAsync(new GPSData(latitude, longitude));
                        }
                    } catch (BufferUnderflowException e){
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                }

                //No need to close the connection, just drop it from the client side
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
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
 *
 * @author Angus Lam
 * @version 4-3-2018
 */
class GPSData {
    /**
     * The GPS latitude
     */
    public double latitude;
    /**
     * The GPS longitude
     */
    public double longitude;

    /**
     * This creates a GPSData "JSON" object
     *
     * @param latitude
     *          The GPS latitude
     * @param longitude
     *          The GPS Longitude
     */
    public GPSData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}