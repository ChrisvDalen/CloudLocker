package org.avans.cloudlocker.cloudlocker.server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CloudLockerServer {

    private static final Logger LOGGER = Logger.getLogger(CloudLockerServer.class.getName());
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.log(Level.INFO,"Server started and waiting for client...");
            Socket clientSocket = serverSocket.accept();
            LOGGER.log(Level.INFO,"Client connected!");

            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read greeting from client
            String message = input.readLine();
            LOGGER.log(Level.INFO,"Received from client: " + message);

            // Send response to client
            output.println("Hello from server!");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Server exception: " + e.getMessage(), e);
        }
    }
}
