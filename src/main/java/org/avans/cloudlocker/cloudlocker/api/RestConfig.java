package org.avans.cloudlocker.cloudlocker.api;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class RestConfig extends ResourceConfig {
    public RestConfig() {
        packages("org.avans.cloudlocker.cloudlocker.api");
    }
}
