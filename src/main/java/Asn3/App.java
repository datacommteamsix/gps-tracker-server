package Asn3;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The Main class.
 *
 * @author Angus Lam
 * @version 4-3-2018
 */
public class App {
    /**
     * The main method.
     *
     * @param args
     *          Only takes in one argument for server port
     */
    public static void main (String[] args) {
        ServerSocket listeningSocket;

        // Initialize Firebase using SNIPPETS
        try {
            FileInputStream serviceAccount = new FileInputStream("comp4985-ass-3-firebase-adminsdk-8h0nb-9ab704e427.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://comp4985-ass-3.firebaseio.com")
                    .build();

            FirebaseApp firebase = FirebaseApp.initializeApp(options);
            System.out.println("" + firebase.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Get the firebase instance
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get port from command line because why not
        if(args.length != 1) {
            System.out.println("Usage Error : java jserver <port>");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]);

        try {
            // Spawn the listening socket
            listeningSocket = new ServerSocket(port);

            System.out.println("Listening socket created: " + listeningSocket.getLocalSocketAddress());

            // Start listening...
            while(true) {
                // Blocking accept call, returns a new socket to handle the connected client
                Socket clientSocket = listeningSocket.accept();

                System.out.println("Server accepted new client: " + clientSocket.getRemoteSocketAddress());

                DatabaseReference ref = database.getReference();
                System.out.println("Creating servant thread");
                // If it unblocks because it has accepted a connect, delegate the work to a new thread, then rinse and repeat
                new ServerThread(clientSocket, ref).start();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}