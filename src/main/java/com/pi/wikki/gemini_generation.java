// File Location: src/main/java/com/pi/wikki/gemini_generation.java
package com.pi.wikki;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.pi.wikki.Wikki.LOGGER;

public class gemini_generation {

    // The specific model we want to use. gemini-1.5-flash is fast and cost-effective.
    private static final String MODEL_ID = "gemini-2.5-flash";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_ID + ":generateContent";

    // Create a single reusable HttpClient.
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /**
     * Generates a content response from the Gemini API with Minecraft-specific context.
     *
     * @param userInput The question asked by the player.
     * @return A string containing the formatted response or an error message.
     */
    public String Generate_content(String userInput) {
        // Step 1: Load the user's API key.
        String apiKey = GeminiConfigManager.loadApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "§cError: Your Gemini API Key is not set. Please use the /geminiconfig command to set it.";
        }

        try {
            // Step 2: Create the JSON payload with system instructions.
            // The system instruction tells the AI its role.
            String jsonPayload = """
            {
              "systemInstruction": {
                "parts": [
                  { "text": "You are a helpful assistant integrated into the game Minecraft. Your name is Wikki. Answer all questions from the perspective of being inside the Minecraft world. Keep your answers concise, short, straightforward, friendly, and useful for a who is currently playing the game." }
                ]
              },
              "contents": [
                {
                  "role": "user",
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ]
            }
            """.formatted(escapeJson(userInput)); // Use formatted() and a helper to safely insert user input.


            // Step 3: Build the HTTP request.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey) // Use the loaded API key here.
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Step 4: Send the request and get the response.
            // The .send() method is synchronous (blocking), which is why we run this whole method on a background thread.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Step 5: Check for errors and parse the response.
            if (response.statusCode() != 200) {
                System.err.println("Gemini API Error: " + response.body());
                return "§cError: Received an error from the API (Code: " + response.statusCode() + ")";
            }
            LOGGER.info(parseResponse(response.body()));

            return parseResponse(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "§cError: Failed to send request to Gemini. Check your internet connection.";
        }
    }

    /**
     * A very simple parser to extract the text content from the Gemini JSON response.
     * This avoids needing a full JSON library like Gson for this specific task.
     * @param responseBody The JSON string from the API.
     * @return The extracted text content.
     */
    private String parseResponse(String responseBody) {
        // A simple but effective way to find the content we need.
        String searchKey = "\"text\": \"";
        int startIndex = responseBody.indexOf(searchKey);
        if (startIndex == -1) {
            return "Could not parse the response from Gemini.";
        }

        startIndex += searchKey.length();
        int endIndex = responseBody.indexOf("\"", startIndex);

        if (endIndex == -1) {
            return "Could not parse the response from Gemini.";
        }

        // Unescape common JSON characters like \n and \"
        return responseBody.substring(startIndex, endIndex)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }

    /**
     * Escapes special characters in user input to prevent breaking the JSON structure.
     * @param input The raw user input string.
     * @return A JSON-safe string.
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}