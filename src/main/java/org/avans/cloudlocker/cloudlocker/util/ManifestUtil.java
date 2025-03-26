package org.avans.cloudlocker.cloudlocker.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

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

    public static void main(String[] args) throws IOException {
        generateManifest("client_sync_folder", "client_manifest.txt");
        System.out.println("Manifest generated.");
    }
}
