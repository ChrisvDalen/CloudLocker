package org.avans.cloudlocker.cloudlocker.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.avans.cloudlocker.cloudlocker.sync.SyncManager;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.concurrent.*;

@Path("/sync")
public class SyncApi {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static volatile boolean syncing = false;

    @POST
    @Path("/start")
    public Response startSync() {
        if (syncing) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Synchronization already running").build();
        }

        syncing = true;
        executor.submit(() -> {
            try {
                SyncManager.main(new String[]{});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            syncing = false;
        });

        return Response.ok("Synchronization started").build();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSyncStatus() {
        return Response.ok("{\"syncing\": " + syncing + "}").build();
    }
}

