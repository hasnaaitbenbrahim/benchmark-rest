package com.example.variantA.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        packages("com.example.variantA.resource", "com.example.variantA.config");
        register(JacksonFeature.class);
        registerInstances(new ObjectMapperContextResolver());
    }
}
