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
    private static final String MODEL_ID = GeminiConfigManager.loadModelName();
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
        String modelInstructoins = GeminiConfigManager.loadModelInstructions();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "§cError: Your Gemini API Key is not set. Please press the G button(or set your key binding in settings) to set it.";
        }

        try {
            // Make sure you have loaded your model instructions from the config
            // I'm assuming you have a variable named 'modelInstructions' with the loaded data.
            // And 'userInput' is the player's chat message.
            // And 'apiKey' is the loaded API key.

            // Step 1: CORRECTLY format the JSON payload with BOTH strings.
            String jsonPayload = """
                {
                  "systemInstruction": {
                    "parts": [
                      { "text": "%s" }
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
                """.formatted(
                    escapeJson(modelInstructoins), // First %s gets the model instructions
                    escapeJson(userInput)        // Second %s gets the user's message
            );


            // Step 2: Build the HTTP request WITHOUT the incorrect header.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();


            // The .send() method is synchronous (blocking), which is why we run this whole method on a background thread.
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Step 5: Check for errors and parse the response.
            if (response.statusCode() != 200) {
                System.err.println("Gemini API Error: " + response.body());
                return "§cError: Received an error from the API (Code: " + response.statusCode() + response.body() + ")";
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