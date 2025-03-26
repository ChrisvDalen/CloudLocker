package org.avans.cloudlocker.cloudlocker.sync;

import org.avans.cloudlocker.cloudlocker.client.CloudLockerClient;
import org.avans.cloudlocker.cloudlocker.util.ManifestUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SyncManager {

    private static final String CLIENT_DIR = "client_sync_folder";
    private static final String SERVER_DIR = "server_files";

    public static void main(String[] args) throws IOException {
        ManifestUtil.generateManifest(CLIENT_DIR, "client_manifest.txt");
        ManifestUtil.generateManifest(SERVER_DIR, "server_manifest.txt");

        Map<String, Long> clientManifest = ManifestUtil.readManifest("client_manifest.txt");
        Map<String, Long> serverManifest = ManifestUtil.readManifest("server_manifest.txt");

        List<String> filesToSync = ManifestUtil.compareManifests(clientManifest, serverManifest);

        for (String file : filesToSync) {
            boolean onClient = clientManifest.containsKey(file);
            boolean onServer = serverManifest.containsKey(file);

            try {
                if (onClient && (!onServer || !clientManifest.get(file).equals(serverManifest.get(file)))) {
                    System.out.println("Uploading file to server: " + file);
                    CloudLockerClient.uploadFile(CLIENT_DIR, file);
                } else if (!onClient && onServer) {
                    System.out.println("Downloading file to client: " + file);
                    CloudLockerClient.downloadFile(file, CLIENT_DIR);
                }
            } catch (IOException e) {
                System.err.println("Sync error for file " + file + ": " + e.getMessage());
            }
        }
    }
}
