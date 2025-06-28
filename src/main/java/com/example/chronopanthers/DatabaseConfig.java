package com.example.chronopanthers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class DatabaseConfig {
    private static final String JDBC_URI = loadJdbcUriFromEnv();

    private static String loadJdbcUriFromEnv() {
        try (Scanner scanner = new Scanner(new FileInputStream(".env"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("SUPABASE_JDBC_URI=")) {
                    String uri = line.substring("SUPABASE_JDBC_URI=".length());
                    System.out.println("✓ Loaded JDBC URI from .env file");
                    return uri;
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ .env file not found, trying system environment variable");
        }

        String envUri = System.getenv("SUPABASE_JDBC_URI");
        if (envUri != null) {
            System.out.println("✓ Loaded JDBC URI from system environment");
            return envUri;
        }

        System.err.println("SUPABASE_JDBC_URI not found in .env file or environment variables!");
        System.err.println("Please create a .env file with: SUPABASE_JDBC_URI=your_database_url");
        throw new RuntimeException("Database configuration not found");
    }

    public static String getJdbcUrl() {
        return JDBC_URI;
    }
}