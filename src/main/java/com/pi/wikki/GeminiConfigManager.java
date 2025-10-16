package com.pi.wikki;

import net.minecraft.client.MinecraftClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GeminiConfigManager {

    // Get the path to .minecraft/config/wikki.properties
    private static final Path CONFIG_FILE = MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("wikki.properties");

    // Saves the API key to the file
    public static void saveApiKey(String apiKey) {
        try {
            // Ensure the config directory exists
            Files.createDirectories(CONFIG_FILE.getParent());
            // Write the key in a simple "key=value" format
            Files.writeString(CONFIG_FILE, "GEMINI_API_KEY=" + apiKey);
        } catch (IOException e) {
            System.err.println("Failed to save Gemini API key:");
            e.printStackTrace();
        }
    }

    // Loads the API key from the file
    public static String loadApiKey() {
        if (!Files.exists(CONFIG_FILE)) {
            return ""; // Return empty string if the file doesn't exist
        }

        try {
            for (String line : Files.readAllLines(CONFIG_FILE)) {
                // Find the line starting with our key
                if (line.startsWith("GEMINI_API_KEY=")) {
                    // Return the value part of the "key=value" pair
                    return line.substring("GEMINI_API_KEY=".length());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load Gemini API key:");
            e.printStackTrace();
        }
        return ""; // Return empty if key not found or on error
    }
}