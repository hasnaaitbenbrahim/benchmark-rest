package com.example.variantA.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.Map;
import java.util.Properties;

public final class JPAUtil {
    private static final EntityManagerFactory emf = buildEntityManagerFactory();

    private JPAUtil() {}

    private static EntityManagerFactory buildEntityManagerFactory() {
        Properties props = new Properties();
        Map<String, String> env = System.getenv();

        // Allow overriding via environment variables
        String url = env.getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
        String user = env.getOrDefault("DB_USER", "postgres");
        String pass = env.getOrDefault("DB_PASSWORD", "postgres");

        props.setProperty("jakarta.persistence.jdbc.url", url);
        props.setProperty("jakarta.persistence.jdbc.user", user);
        props.setProperty("jakarta.persistence.jdbc.password", pass);
        props.setProperty("hibernate.hikari.minimumIdle", System.getProperty("hikari.minIdle", "10"));
        props.setProperty("hibernate.hikari.maximumPoolSize", System.getProperty("hikari.maxPoolSize", "20"));

        return Persistence.createEntityManagerFactory("app-pu", props);
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
