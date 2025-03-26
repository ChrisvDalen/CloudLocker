package org.avans.cloudlocker.cloudlocker.client;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudLockerClient {

    private static final Logger LOGGER = Logger.getLogger(CloudLockerClient.class.getName());
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            LOGGER.log(Level.INFO,"Connected to server!");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send greeting to server
            output.println("Hello from client!");

            // Read response from server
            String response = input.readLine();
            LOGGER.log(Level.INFO,"Received from server: " + response);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Client exception: " + e.getMessage());
        }
    }
}
