// File Location: src/main/java/com/pi/wikki/client/gui/GeminiConfigScreen.java
package com.pi.wikki.client.gui;

import com.pi.wikki.GeminiConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class GeminiConfigScreen extends Screen {
    // 1. Declare the field here, but DO NOT initialize it yet.
    private TextFieldWidget apiKeyField;

    // We store the parent screen to return to it, although we are closing it for now.
    // It's good practice in case you want a "Back" button later.
    public GeminiConfigScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        // This method is called when the screen opens or is resized.
        // This is the PERFECT place to create and position our widgets.

        // --- KEY CHANGE: Make the text field much wider ---
        // We calculate a width that leaves a nice margin on both sides of the screen.
        int textFieldWidth = this.width - 100; // e.g., 50px margin on left and right
        int textFieldX = this.width / 2 - textFieldWidth / 2; // Center the wide field

        // 2. Initialize the widget here, inside init(), using the calculated dimensions.
        this.apiKeyField = new TextFieldWidget(
                this.textRenderer,
                textFieldX, // Use the new centered X position
                60,         // Y position (moved down a bit for space)
                textFieldWidth, // Use the new, much wider width
                20,         // Height
                Text.literal("Paste your Gemini API Key here...")
        );

        this.apiKeyField.setMaxLength(128);
        // Load the currently saved API key into the text field
        this.apiKeyField.setText(GeminiConfigManager.loadApiKey());
        this.addDrawableChild(this.apiKeyField);

        // Set the focus so the player can start typing immediately
        this.setInitialFocus(this.apiKeyField);

        // --- SAVE BUTTON ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save and Close"), button -> {
            String newApiKey = this.apiKeyField.getText();
            GeminiConfigManager.saveApiKey(newApiKey);

            if (this.client != null && this.client.player != null) {
                this.client.player.sendMessage(Text.literal("§aGemini API Key saved!"), false);
            }
            this.close(); // Close the screen after saving
        }).dimensions(this.width / 2 - 75, 90, 150, 20).build()); // Centered the button
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta); // Renders the background tint
        // Draw titles for the screen
        context.drawCenteredTextWithShadow(this.textRenderer, "Gemini API Configuration", this.width / 2, 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Get your key from Google AI Studio", this.width / 2, 32, 0xAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}