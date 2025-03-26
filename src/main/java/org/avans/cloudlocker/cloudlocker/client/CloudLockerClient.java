package org.avans.cloudlocker.cloudlocker.client;

import java.io.*;
import java.net.*;

public class CloudLockerClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 5000; // 5 seconds timeout

    public static void uploadFile(String filePath, String fileName) throws IOException {
        File file = new File(filePath, fileName);

        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(SERVER_ADDRESS, PORT), TIMEOUT_MS);
                socket.setSoTimeout(TIMEOUT_MS);

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF("UPLOAD");
                dos.writeUTF(fileName);
                dos.writeLong(file.length());

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, count);
                    }
                }

                String response = dis.readUTF();
                if ("UPLOAD_SUCCESS".equals(response)) {
                    System.out.println("Upload success: " + fileName);
                    success = true;
                }

            } catch (IOException e) {
                attempt++;
                System.err.println("Upload attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    System.out.println("Retrying upload...");
                    try {
                        Thread.sleep(2000); // Wait 2 seconds before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new IOException("Upload failed after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }

    public static void downloadFile(String fileName, String targetPath) throws IOException {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(SERVER_ADDRESS, PORT), TIMEOUT_MS);
                socket.setSoTimeout(TIMEOUT_MS);

                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF("DOWNLOAD");
                dos.writeUTF(fileName);

                String status = dis.readUTF();
                if ("FILE_FOUND".equals(status)) {
                    long filesize = dis.readLong();

                    File outputFile = new File(targetPath, fileName);
                    outputFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[4096];
                        long totalRead = 0;
                        int read;

                        while (totalRead < filesize) {
                            read = dis.read(buffer, 0, (int) Math.min(buffer.length, filesize - totalRead));
                            fos.write(buffer, 0, read);
                            totalRead += read;
                        }

                        System.out.println("Downloaded file: " + fileName);
                        success = true;
                    }
                } else {
                    System.out.println("Server response: File not found - " + fileName);
                    success = true; // Exit loop as retry won't fix missing file
                }

            } catch (IOException e) {
                attempt++;
                System.err.println("Download attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    System.out.println("Retrying download...");
                    try {
                        Thread.sleep(2000); // Wait 2 seconds before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    throw new IOException("Download failed after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }
}
