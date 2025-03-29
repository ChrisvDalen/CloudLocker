package org.avans.cloudlocker.cloudlocker.client;

import java.io.*;
import java.net.*;

public class CloudLockerClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_MS = 5000; // 5 seconds timeout

    private static final int CHUNK_SIZE = 10 * 1024 * 1024; // 10MB chunks

    public static void uploadFile(String filePath, String fileName) throws IOException, InterruptedException {
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
                    long remaining = file.length();
                    int read;

                    while (remaining > 0) {
                        int chunkSize = (int) Math.min(CHUNK_SIZE, remaining);
                        int chunkSent = 0;

                        while (chunkSent < chunkSize && (read = fis.read(buffer, 0, Math.min(buffer.length, chunkSize - chunkSent))) > 0) {
                            dos.write(buffer, 0, read);
                            chunkSent += read;
                        }
                        dos.flush();
                        dis.readUTF(); // Wait for server acknowledgment
                        remaining -= chunkSent;
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
                    Thread.sleep(2000);
                } else {
                    throw new IOException("Upload failed after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }

    public static void downloadFile(String fileName, String targetPath) throws IOException, InterruptedException {
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
                        long remaining = filesize;
                        int read;

                        while (remaining > 0) {
                            int chunkSize = (int)Math.min(CHUNK_SIZE, remaining);
                            int chunkRead = 0;

                            while (chunkRead < chunkSize) {
                                read = dis.read(buffer, 0, Math.min(buffer.length, chunkSize - chunkRead));
                                if (read == -1) break;
                                fos.write(buffer, 0, read);
                                chunkRead += read;
                            }
                            dos.writeUTF("CHUNK_RECEIVED");
                            remaining -= chunkRead;
                        }
                    }
                    System.out.println("Downloaded file: " + fileName);
                    success = true;
                } else {
                    System.out.println("Server response: File not found - " + fileName);
                    success = true;
                }

            } catch (IOException e) {
                attempt++;
                System.err.println("Download attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    System.out.println("Retrying download...");
                    Thread.sleep(2000);
                } else {
                    throw new IOException("Download failed after " + MAX_RETRIES + " attempts.");
                }
            }
        }
    }

}
