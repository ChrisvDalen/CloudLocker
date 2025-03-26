package org.avans.cloudlocker.cloudlocker.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ManifestUtil {

    public static void generateManifest(String directoryPath, String manifestFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(manifestFilePath));
        Path rootPath = Paths.get(directoryPath);

        Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                long modifiedTime = attrs.lastModifiedTime().toMillis();
                String relativePath = rootPath.relativize(file).toString();
                writer.write(relativePath + "," + modifiedTime);
                writer.newLine();
                return FileVisitResult.CONTINUE;
            }
        });

        writer.close();
    }

    public static Map<String, Long> readManifest(String manifestFilePath) throws IOException {
        Map<String, Long> manifest = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(manifestFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                manifest.put(parts[0], Long.parseLong(parts[1]));
            }
        }
        return manifest;
    }

    public static List<String> compareManifests(Map<String, Long> clientManifest, Map<String, Long> serverManifest) {
        List<String> filesToSync = new ArrayList<>();

        for (String file : clientManifest.keySet()) {
            if (!serverManifest.containsKey(file) || !clientManifest.get(file).equals(serverManifest.get(file))) {
                filesToSync.add(file); // New or updated on client
            }
        }

        for (String file : serverManifest.keySet()) {
            if (!clientManifest.containsKey(file)) {
                filesToSync.add(file); // File missing on client
            }
        }

        return filesToSync;
    }

    public static void main(String[] args) throws IOException {
        // Generate Client Manifest
        generateManifest("client_sync_folder", "client_manifest.txt");

        // Generate Server Manifest (just for testing now)
        generateManifest("server_files", "server_manifest.txt");

        // Read manifests
        Map<String, Long> client = readManifest("client_manifest.txt");
        Map<String, Long> server = readManifest("server_manifest.txt");

        // Compare manifests
        List<String> filesToSync = compareManifests(client, server);
        System.out.println("Files needing synchronization:");
        filesToSync.forEach(System.out::println);
    }
}
