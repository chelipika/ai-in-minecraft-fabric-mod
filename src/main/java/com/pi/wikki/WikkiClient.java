package com.pi.wikki;

// Make sure to import both of your screen classes
import com.pi.wikki.client.gui.CustomScreen;
import com.pi.wikki.client.gui.GeminiConfigScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class WikkiClient implements ClientModInitializer {

    // --- YOUR FIX INCORPORATED: Create a single, reusable category ---
    // This is the best practice. Well done!
    private static final KeyBinding.Category WIKKI_CATEGORY = KeyBinding.Category
            .create(Identifier.of("wikki", "key.category.wikki"));

    // --- Keybinding to open the CONFIG screen ---
    private static final KeyBinding openConfigKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.wikki.open_config",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_G, // Default: 'G' key
                    WIKKI_CATEGORY
            )
    );

    // --- NEW: Keybinding to open the ASK A QUESTION screen ---
    private static final KeyBinding openAskScreenKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.wikki.open_ask_screen",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_J, // Default: 'J' key
                    WIKKI_CATEGORY
            )
    );

    @Override
    public void onInitializeClient() {
        // Register a tick event to listen for our key presses
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Check if the CONFIG key was pressed
            while (openConfigKeybind.wasPressed()) {
                // Ensure we are in-game and not on another screen
                if (client.world != null && client.currentScreen == null) {
                    client.setScreen(new GeminiConfigScreen(Text.literal("Gemini API Config")));
                }
            }

            // --- NEW: Check if the ASK key was pressed ---
            while (openAskScreenKeybind.wasPressed()) {
                // Ensure we are in-game and not on another screen
                if (client.world != null && client.currentScreen == null) {
                    // Open the CustomScreen for asking questions
                    client.setScreen(new CustomScreen(Text.literal("Ask Gemini")));
                }
            }
        });
    }
}