package org.avans.cloudlocker.cloudlocker.api;

package org.avans.cloudlocker.cloudlocker.api;

import jakarta.ws.rs.core.Application;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import static sun.net.www.protocol.http.AuthCacheValue.Type.Server;

public class ApiLauncher {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(server, "/api");

        ServletContainer servletContainer = new ServletContainer(new RestConfig());
        context.addServlet(() -> servletContainer, "/*")
                .setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, RestConfig.class.getName());

        server.start();
        System.out.println("API running on http://localhost:8080/api/sync/status");
        server.join();
    }
}

