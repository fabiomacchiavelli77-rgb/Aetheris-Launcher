package net.aetheris.client.gui;

import net.aetheris.client.account.SessionManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AltManagerScreen extends Screen {
    private final Screen parent;
    private EditBox usernameInput;
    private String statusMessage = "";

    public AltManagerScreen(Screen parent) {
        super(Component.literal("Alt Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();

        // Input field per nuovo Alt
        this.usernameInput = new EditBox(this.font, this.width / 2 - 120, 50, 160, 20, Component.literal("Username"));
        this.usernameInput.setMaxLength(16);
        this.addRenderableWidget(this.usernameInput);

        // Bottone aggiungi Alt
        this.addRenderableWidget(Button.builder(Component.literal("Add Alt"), b -> {
            String text = usernameInput.getValue().trim();
            if (!text.isEmpty()) {
                SessionManager.getInstance().addAlt(text);
                usernameInput.setValue("");
                statusMessage = "§aAdded alt: " + text;
                rebuildAltButtons();
            }
        }).bounds(this.width / 2 + 45, 50, 75, 20).build());

        rebuildAltButtons();

        // Bottone Back
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    private void rebuildAltButtons() {
        // Rimuove vecchi widget tranne input e add button
        this.clearWidgets();
        this.addRenderableWidget(this.usernameInput);
        this.addRenderableWidget(Button.builder(Component.literal("Add Alt"), b -> {
            String text = usernameInput.getValue().trim();
            if (!text.isEmpty()) {
                SessionManager.getInstance().addAlt(text);
                usernameInput.setValue("");
                statusMessage = "§aAdded alt: " + text;
                rebuildAltButtons();
            }
        }).bounds(this.width / 2 + 45, 50, 75, 20).build());

        int y = 85;
        for (String alt : SessionManager.getInstance().getAlts()) {
            boolean isCurrent = alt.equalsIgnoreCase(SessionManager.getInstance().getCurrentAlt());
            String label = (isCurrent ? "§a✓ " : "") + alt;

            // Bottone login/select
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                SessionManager.getInstance().setSession(alt);
                statusMessage = "§aSwitched session to: " + alt;
                rebuildAltButtons();
            }).bounds(this.width / 2 - 120, y, 180, 20).build());

            // Bottone delete
            String finalAlt = alt;
            this.addRenderableWidget(Button.builder(Component.literal("§cX"), b -> {
                SessionManager.getInstance().removeAlt(finalAlt);
                statusMessage = "§cRemoved alt: " + finalAlt;
                rebuildAltButtons();
            }).bounds(this.width / 2 + 65, y, 55, 20).build());

            y += 24;
            if (y > this.height - 60) break;
        }

        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(this.font, "§d§lAETHERIS ALT MANAGER", this.width / 2, 12, 0xFFFFFFFF);
        g.drawCenteredString(this.font, "Logged in as: §e" + SessionManager.getInstance().getCurrentAlt(), this.width / 2, 30, 0xFFAAAAAA);

        if (!statusMessage.isEmpty()) {
            g.drawCenteredString(this.font, statusMessage, this.width / 2, this.height - 45, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
