package com.example.chronopanthers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class AIConfig {
    private static final String OPENROUTER_API_KEY = loadApiKeyFromEnv();

    private static String loadApiKeyFromEnv() {
        // First try to load from .env file
        try (Scanner scanner = new Scanner(DatabaseConfig.class.getClassLoader().getResourceAsStream(".env"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("OPENROUTER_API_KEY=")) {
                    String key = line.substring("OPENROUTER_API_KEY=".length());
                    //System.out.println("✓ Loaded OpenRouter API key from .env file");
                    return key;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ .env file not found, trying system environment variable");
        }

        // Try system environment variable
        String envKey = System.getenv("OPENROUTER_API_KEY");
        if (envKey != null) {
            //System.out.println("✓ Loaded OpenRouter API key from system environment");
            return envKey;
        }

        System.err.println("OPENROUTER_API_KEY not found in .env file or environment variables!");
        System.err.println("Please create a .env file with: OPENROUTER_API_KEY=your_api_key");
        throw new RuntimeException("OpenRouter API key not found");
    }

    public static String getApiKey() {
        return OPENROUTER_API_KEY;
    }
}