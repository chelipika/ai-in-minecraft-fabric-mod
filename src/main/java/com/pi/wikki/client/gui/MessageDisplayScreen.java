package com.pi.wikki.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class MessageDisplayScreen extends Screen {
    private static final int LINE_HEIGHT = 12;

    private final String fullMessage;
    private List<String> lines = new ArrayList<>();
    private int scrollOffset = 0;

    private ButtonWidget backButton;
    private ButtonWidget copyButton;

    public MessageDisplayScreen(String message) {
        super(Text.literal("Gemini Response"));
        this.fullMessage = message == null ? "(empty response)" : message;
    }

    @Override
    protected void init() {
        this.lines = wrapText(this.fullMessage, this.width - 20);

        this.backButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(this.width / 2 - 100, this.height - 25, 60, 20).build());

        this.copyButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Copy"), b -> {
            if (this.client != null) {
                this.client.keyboard.setClipboard(this.fullMessage);
            }
        }).dimensions(this.width / 2 + 40, this.height - 25, 60, 20).build());

        System.out.println("[DEBUG] Gemini lines = " + lines.size());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int maxScroll = Math.max(0, lines.size() - ((this.height - 60) / LINE_HEIGHT));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - vertical));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark transparent background
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // Lazy init fallback
        if (lines == null || lines.isEmpty()) {
            lines = wrapText(fullMessage, this.width - 20);
        }

        // Draw each visible line
        int y = 20;
        int visibleLines = (this.height - 60) / LINE_HEIGHT;
        for (int i = 0; i < visibleLines && (i + scrollOffset) < lines.size(); i++) {
            String line = lines.get(i + scrollOffset);
            // Make sure alpha is full (0xFFFFFFFF)
            context.drawText(this.textRenderer, line, 10, y, 0xFFFFFFFF, false);
            y += LINE_HEIGHT;
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> out = new ArrayList<>();
        if (text == null || text.isEmpty()) return out;

        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String test = line + word + " ";
                if (this.textRenderer.getWidth(test) > maxWidth) {
                    out.add(line.toString());
                    line = new StringBuilder(word).append(" ");
                } else {
                    line.append(word).append(" ");
                }
            }
            if (!line.isEmpty()) out.add(line.toString());
            out.add(""); // blank between paragraphs
        }

        return out;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
