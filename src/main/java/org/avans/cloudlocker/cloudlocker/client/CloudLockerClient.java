package org.avans.cloudlocker.cloudlocker.client;

import java.io.*;
import java.net.*;

public class CloudLockerClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void uploadFile(String filePath, String fileName) throws IOException {
        File file = new File(filePath, fileName);

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF("UPLOAD");
            dos.writeUTF(fileName);
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, count);
            }

            String response = dis.readUTF();
            System.out.println("Upload Response: " + response);
        }
    }

    public static void downloadFile(String fileName, String targetPath) throws IOException {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

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
                }
            } else {
                System.out.println("Server response: File not found - " + fileName);
            }
        }
    }
}
