package org.avans.cloudlocker.cloudlocker.client;

import java.io.*;
import java.net.*;

public class CloudLockerClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final String FILE_TO_UPLOAD = "test_upload.txt";
    private static final String DOWNLOAD_TARGET = "test_download.txt";

    public static void main(String[] args) {
        uploadFile(FILE_TO_UPLOAD);
        downloadFile(FILE_TO_UPLOAD, DOWNLOAD_TARGET);
    }

    private static void uploadFile(String filename) {
        File file = new File(filename);

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF("UPLOAD");
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int count;

            while ((count = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, count);
            }

            String response = dis.readUTF();
            System.out.println("Upload Response: " + response);

        } catch (IOException e) {
            System.err.println("Client upload error: " + e.getMessage());
        }
    }

    private static void downloadFile(String filename, String target) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            dos.writeUTF("DOWNLOAD");
            dos.writeUTF(filename);

            String status = dis.readUTF();
            if ("FILE_FOUND".equals(status)) {
                long filesize = dis.readLong();

                try (FileOutputStream fos = new FileOutputStream(target)) {
                    byte[] buffer = new byte[4096];
                    long totalRead = 0;
                    int read;

                    while (totalRead < filesize) {
                        read = dis.read(buffer, 0, (int)Math.min(buffer.length, filesize - totalRead));
                        fos.write(buffer, 0, read);
                        totalRead += read;
                    }

                    System.out.println("Downloaded file: " + target);
                }
            } else {
                System.out.println("Server response: File not found.");
            }

        } catch (IOException e) {
            System.err.println("Client download error: " + e.getMessage());
        }
    }
}
