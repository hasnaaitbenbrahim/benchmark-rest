package com.example.variantA.config;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class App {
    public static final String BASE_URI = System.getProperty("server.baseUri", "http://0.0.0.0:8080/");

    public static void main(String[] args) throws IOException {
        final ResourceConfig rc = new JerseyConfig();
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        System.out.println("Jersey app started at " + BASE_URI + " (Press Ctrl+C to stop)");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }
}
