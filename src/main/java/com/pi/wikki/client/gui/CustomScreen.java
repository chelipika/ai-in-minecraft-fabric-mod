// File Location: src/main/java/com/pi/wikki/client/gui/CustomScreen.java
package com.pi.wikki.client.gui;

import com.pi.wikki.gemini_generation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW; // <-- IMPORTANT: We need this for key codes

import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import java.util.concurrent.CompletableFuture;

public class CustomScreen extends Screen {
    private TextFieldWidget questionTextField;
    private final gemini_generation gemini = new gemini_generation();

    public CustomScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init(); // Call superclass method

        // --- TEXT INPUT FIELD ---
        this.questionTextField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 100, // Center X
                40,                  // Y position
                200,                 // Width
                20,                  // Height
                Text.literal("Enter your question...")
        );
        this.questionTextField.setMaxLength(999);
        this.addDrawableChild(this.questionTextField);
        this.setInitialFocus(this.questionTextField);

        // --- SUBMIT BUTTON ---
        // The button now calls our new, reusable method
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Ask Gemini"), button -> {
            this.handleGeminiRequest();
        }).dimensions(this.width / 2 - 50, 70, 100, 20).build());
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        // leave the screen if ESC is pressed
        if (input.key() == GLFW.GLFW_KEY_ESCAPE){
            return super.keyPressed(input);
        }


        // Check for Enter or Numpad Enter
        if (this.questionTextField.isFocused() &&
                (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER)) {

            this.handleGeminiRequest();
            return true;
        }

        // Let the text field handle normal typing/backspace etc.
        if (this.questionTextField.keyPressed(input) || this.questionTextField.isActive()) {
            return true;
        }

        // Fall back to default Screen behavior
        return super.keyPressed(input);
    }


    /**
     * A single, reusable method to handle the logic of sending a prompt to Gemini.
     * This is called by both the button and the Enter key press.
     */
    private void handleGeminiRequest() {
        String userInput = this.questionTextField.getText();
        if (userInput.isBlank() || this.client == null || this.client.player == null) {
            return; // Don't do anything if there's no input
        }

        // Give immediate feedback and close the screen
        this.client.player.sendMessage(Text.literal("§7Asking Gemini... please wait."), false);
        this.close();

        // Start the API call on a background thread
        CompletableFuture.runAsync(() -> {
            try {
                String response = gemini.Generate_content(userInput);
                if (response == null || response.isBlank()) {
                    response = "Received an empty response from Gemini.";
                }

                String finalResponse = "§aGemini: §f" + response;

                // Schedule the response message back on the main game thread
                this.client.execute(() -> {
                    if (this.client.player != null) {
                        this.client.setScreen(new MessageDisplayScreen(finalResponse));

                    }
                });

            } catch (Exception e) {
                this.client.execute(() -> {
                    if (this.client.player != null) {
                        this.client.player.sendMessage(Text.literal("§cError: Could not get a response from Gemini."), false);
                    }
                });
                e.printStackTrace();
            }
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "Ask a Question", this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}