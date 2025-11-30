package com.pi.wikki.client.gui;

import com.pi.wikki.GeminiConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;



public class MessageDisplayScreen extends Screen {
    private int BG_COLOR;
    private static final int LINE_HEIGHT = 12;
    // Define margins to make code cleaner
    private static final int TOP_MARGIN = 30;
    private static final int BOTTOM_MARGIN = 30;
    private static final int SIDE_MARGIN = 20;

    private final String fullMessage;
    // --- CHANGE 1: Changed the list type from String to OrderedText ---
    private List<OrderedText> lines = new ArrayList<>();
    private int scrollOffset = 0;

    private ButtonWidget backButton;
    private ButtonWidget copyButton;

    public MessageDisplayScreen(String message) {
        super(Text.literal("Gemini Response"));
        System.out.println("[DEBUG] Gemini Raw Message: " + message);
        this.fullMessage = message == null ? "(empty response)" : message;
    }

    @Override
    protected void init() {
        int wrapWidth = this.width - (SIDE_MARGIN * 2);
        this.lines = wrapText(this.fullMessage, wrapWidth);

        this.backButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            if (this.client != null) this.client.setScreen(null);
        }).dimensions(this.width / 2 - 100, this.height - 25, 60, 20).build());

        this.copyButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Copy"), b -> {
            if (this.client != null) {
                this.client.keyboard.setClipboard(this.fullMessage);
            }
        }).dimensions(this.width / 2 + 40, this.height - 25, 60, 20).build());

        this.scrollOffset = 0;
        System.out.println("[DEBUG] Gemini lines = " + lines.size());
        String bbgsdfk = GeminiConfigManager.loadBgColor().substring(2,10); // FFFFA600
        System.out.println(bbgsdfk);
        BG_COLOR = Integer.parseUnsignedInt(bbgsdfk,16);
    }

    private int getVisibleLines() {
        int textHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        return textHeight / LINE_HEIGHT;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int maxScroll = Math.max(0, this.lines.size() - getVisibleLines());
        this.scrollOffset = (int) Math.max(0, Math.min(maxScroll, this.scrollOffset - vertical));
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // DO NOT call this.renderBackground() here. It's done automatically.
//        context.fill(0, 0, this.width, this.height, BG_COLOR);
        // 1. Draw your custom content (the text)
        int y = TOP_MARGIN;
        int visibleLineCount = getVisibleLines();
        for (int i = 0; i < visibleLineCount && (i + this.scrollOffset) < this.lines.size(); i++) {
            OrderedText line = this.lines.get(i + this.scrollOffset);
            context.drawTextWithShadow(this.textRenderer, line, SIDE_MARGIN, y, 0xFFFFFFFF);
            y += LINE_HEIGHT;
        }

        // 2. Draw the widgets (buttons, etc.) on top of everything.
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Correctly wraps text using the TextRenderer's built-in functionality.
     * This now returns a List<OrderedText> which is the native format for rendering.
     */
    // --- CHANGE 3: Changed the method's return type ---
    private List<OrderedText> wrapText(String text, int maxWidth) {
        if (text == null || text.isEmpty() || this.textRenderer == null) {
            return new ArrayList<>();
        }
        // No stream/map needed. Just return the list directly.
        return this.textRenderer.wrapLines(Text.literal(text), maxWidth);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}