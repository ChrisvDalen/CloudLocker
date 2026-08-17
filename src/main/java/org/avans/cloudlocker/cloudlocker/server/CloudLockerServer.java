package org.avans.cloudlocker.cloudlocker.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CloudLockerServer {

    private static final int PORT = 12345;
    private static final String STORAGE_DIR = "server_files";
    private static final ExecutorService executor = Executors.newFixedThreadPool(10); // handles up to 10 concurrent clients

    public static void main(String[] args) {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Concurrent Server started, waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from: " + clientSocket.getInetAddress());

                // Handle each client concurrently
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            String command = dis.readUTF();

            if ("UPLOAD".equalsIgnoreCase(command)) {
                String filename = dis.readUTF();
                long filesize = dis.readLong();

                try (FileOutputStream fos = new FileOutputStream(STORAGE_DIR + "/" + filename)) {
                    byte[] buffer = new byte[4096];
                    long totalRead = 0;
                    int read;

                    while (totalRead < filesize) {
                        read = dis.read(buffer, 0, (int) Math.min(buffer.length, filesize - totalRead));
                        fos.write(buffer, 0, read);
                        totalRead += read;
                    }

                    System.out.println("File uploaded: " + filename);
                    dos.writeUTF("UPLOAD_SUCCESS");
                }

            } else if ("DOWNLOAD".equalsIgnoreCase(command)) {
                String filename = dis.readUTF();
                File file = new File(STORAGE_DIR + "/" + filename);

                if (file.exists()) {
                    dos.writeUTF("FILE_FOUND");
                    dos.writeLong(file.length());

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int count;

                        while ((count = fis.read(buffer)) > 0) {
                            dos.write(buffer, 0, count);
                        }
                    }
                } else {
                    dos.writeUTF("FILE_NOT_FOUND");
                }
            }

            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());

        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        }
    }
}
