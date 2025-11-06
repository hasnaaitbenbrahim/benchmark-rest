package com.example.variantA.config;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider; // provided by jersey-media-json-jackson
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        packages("com.example.variantA.resource");
        register(JacksonJaxbJsonProvider.class);
    }
}
