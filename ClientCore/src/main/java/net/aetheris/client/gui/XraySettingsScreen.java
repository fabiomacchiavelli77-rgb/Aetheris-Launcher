package net.aetheris.client.gui;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Premium Xray Settings screen with opacity slider, night vision toggle,
 * active block count, and quick access to the full block selector.
 *
 * Inspired by Aristois and Wurst client Xray settings panels.
 */
public class XraySettingsScreen extends Screen {
    private final Screen parent;

    // ── Slider state ─────────────────────────────────────────────────
    private boolean draggingSlider = false;
    private int sliderX, sliderY, sliderW, sliderH;

    // ── Layout constants ─────────────────────────────────────────────
    private static final int CARD_W = 280;
    private static final int CARD_PAD = 16;
    private static final int ROW_H = 22;

    public XraySettingsScreen(Screen parent) {
        super(Component.literal("Xray Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Nothing to add as widgets — everything is custom-drawn
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // ── Dark overlay background ──────────────────────────────────
        g.fill(0, 0, width, height, 0xC0101018);

        int cardH = 260;
        int cx = (width - CARD_W) / 2;
        int cy = (height - cardH) / 2;

        // ── Card background with border ──────────────────────────────
        g.fill(cx - 1, cy - 1, cx + CARD_W + 1, cy + cardH + 1, 0xFF3A3A50); // border
        g.fill(cx, cy, cx + CARD_W, cy + cardH, 0xFF1A1A28); // card bg

        // ── Title ────────────────────────────────────────────────────
        g.drawCenteredString(font, "§6§l⛏ XRAY SETTINGS", cx + CARD_W / 2, cy + 10, 0xFFFFFFFF);

        // ── Accent line under title ──────────────────────────────────
        int accentColor = 0xFFFF8C00;
        g.fill(cx + CARD_PAD, cy + 24, cx + CARD_W - CARD_PAD, cy + 25, accentColor);

        int y = cy + 34;

        // ── Section 1: Opacity Slider ────────────────────────────────
        g.drawString(font, "§e🔍 Trasparenza Blocchi / Block Opacity", cx + CARD_PAD, y, 0xFFFFFFFF);
        y += 14;

        // Slider track
        sliderX = cx + CARD_PAD;
        sliderY = y;
        sliderW = CARD_W - CARD_PAD * 2 - 50;
        sliderH = 14;

        // Track background (dark groove)
        g.fill(sliderX, sliderY + 3, sliderX + sliderW, sliderY + sliderH - 3, 0xFF0A0A14);
        g.fill(sliderX, sliderY + 3, sliderX + sliderW, sliderY + 4, 0xFF2A2A40);

        // Filled portion (gradient effect from green to orange to red)
        int opacity = Xray.getOpacity();
        int fillW = (int) (sliderW * opacity / 100.0);
        int fillColor;
        if (opacity < 30) {
            fillColor = 0xFF00CC66; // green for low opacity = blocks hidden
        } else if (opacity < 70) {
            fillColor = 0xFFFFAA00; // orange for medium
        } else {
            fillColor = 0xFFFF4444; // red for high opacity = blocks visible
        }
        if (fillW > 0) {
            g.fill(sliderX, sliderY + 3, sliderX + fillW, sliderY + sliderH - 3, fillColor);
        }

        // Thumb handle
        int thumbX = sliderX + fillW - 3;
        thumbX = Math.max(sliderX - 3, Math.min(thumbX, sliderX + sliderW - 3));
        g.fill(thumbX, sliderY, thumbX + 6, sliderY + sliderH, 0xFFFFFFFF);
        g.fill(thumbX + 1, sliderY + 1, thumbX + 5, sliderY + sliderH - 1, fillColor);

        // Percentage text
        String pctText = opacity + "%";
        g.drawString(font, "§f" + pctText, sliderX + sliderW + 8, sliderY + 3, 0xFFFFFFFF);

        y += sliderH + 6;

        // Opacity description
        String descText;
        if (opacity == 0) {
            descText = "§a■ Blocchi nascosti completamente (Hidden)";
        } else if (opacity < 30) {
            descText = "§a■ Blocchi quasi invisibili (Ghost)";
        } else if (opacity < 70) {
            descText = "§6■ Blocchi semi-trasparenti (Semi-transparent)";
        } else if (opacity < 100) {
            descText = "§c■ Blocchi quasi visibili (Visible)";
        } else {
            descText = "§c■ Blocchi completamente visibili (Full)";
        }
        g.drawString(font, descText, cx + CARD_PAD, y, 0xFFFFFFFF);
        y += 18;

        // ── Divider ─────────────────────────────────────────────────
        g.fill(cx + CARD_PAD, y, cx + CARD_W - CARD_PAD, y + 1, 0xFF333348);
        y += 8;

        // ── Section 2: Night Vision Toggle ───────────────────────────
        boolean nv = Xray.isNightVisionEnabled();
        String nvLabel = nv ? "§a[✔] Night Vision: ON" : "§c[✖] Night Vision: OFF";
        int nvBtnW = CARD_W - CARD_PAD * 2;
        int nvBtnX = cx + CARD_PAD;
        int nvBtnY = y;
        int nvBtnH = 20;

        // Button background
        boolean nvHover = mouseX >= nvBtnX && mouseX < nvBtnX + nvBtnW
                && mouseY >= nvBtnY && mouseY < nvBtnY + nvBtnH;
        g.fill(nvBtnX, nvBtnY, nvBtnX + nvBtnW, nvBtnY + nvBtnH,
                nvHover ? 0xFF3A3A55 : 0xFF252538);
        g.fill(nvBtnX, nvBtnY, nvBtnX + nvBtnW, nvBtnY + 1,
                nv ? 0xFF00CC66 : 0xFF444460); // top accent
        g.drawCenteredString(font, nvLabel, nvBtnX + nvBtnW / 2, nvBtnY + 6, 0xFFFFFFFF);
        y += nvBtnH + 8;

        // ── Divider ─────────────────────────────────────────────────
        g.fill(cx + CARD_PAD, y, cx + CARD_W - CARD_PAD, y + 1, 0xFF333348);
        y += 8;

        // ── Section 3: Active blocks info + Block Selector Button ────
        int activeCount = Xray.getXrayBlocks().size();
        g.drawString(font, "§f⛏ Blocchi attivi / Active blocks: §e" + activeCount, cx + CARD_PAD, y, 0xFFFFFFFF);
        y += 14;

        // Show a few icons of active blocks
        int iconX = cx + CARD_PAD;
        int iconCount = 0;
        for (Block b : Xray.getXrayBlocks()) {
            if (iconCount >= 12) break; // max 12 icons
            g.renderFakeItem(new ItemStack(b), iconX, y);
            iconX += 18;
            iconCount++;
        }
        if (iconCount > 0) y += 20;

        // "Seleziona Blocchi" button
        int selBtnW = CARD_W - CARD_PAD * 2;
        int selBtnX = cx + CARD_PAD;
        int selBtnY = y;
        int selBtnH = 22;

        boolean selHover = mouseX >= selBtnX && mouseX < selBtnX + selBtnW
                && mouseY >= selBtnY && mouseY < selBtnY + selBtnH;
        g.fill(selBtnX, selBtnY, selBtnX + selBtnW, selBtnY + selBtnH,
                selHover ? 0xFF3A4A60 : 0xFF252840);
        g.fill(selBtnX, selBtnY, selBtnX + selBtnW, selBtnY + 1, 0xFFFF8C00); // top accent
        g.drawCenteredString(font, "§6⛏ Seleziona Blocchi / Block Selector ⚙",
                selBtnX + selBtnW / 2, selBtnY + 7, 0xFFFFFFFF);
        y += selBtnH + 12;

        // ── Divider ─────────────────────────────────────────────────
        g.fill(cx + CARD_PAD, y, cx + CARD_W - CARD_PAD, y + 1, 0xFF333348);
        y += 8;

        // ── Back button ─────────────────────────────────────────────
        int backW = 100;
        int backX = cx + (CARD_W - backW) / 2;
        int backY = y;
        int backH = 20;

        boolean backHover = mouseX >= backX && mouseX < backX + backW
                && mouseY >= backY && mouseY < backY + backH;
        g.fill(backX, backY, backX + backW, backY + backH,
                backHover ? 0xFF444460 : 0xFF2A2A40);
        g.drawCenteredString(font, "§7← Back", backX + backW / 2, backY + 6, 0xFFCCCCCC);

        // ── Close hint ──────────────────────────────────────────────
        g.drawCenteredString(font, "§8ESC per chiudere", cx + CARD_W / 2, cy + cardH - 12, 0xFF666666);
    }

    // ── Mouse interaction ────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);

        int cardH = 260;
        int cx = (width - CARD_W) / 2;
        int cy = (height - cardH) / 2;

        // Slider click
        if (mx >= sliderX && mx < sliderX + sliderW && my >= sliderY - 2 && my < sliderY + sliderH + 2) {
            draggingSlider = true;
            updateSliderValue(mx);
            return true;
        }

        // Night Vision toggle
        int y = cy + 34 + 14 + sliderH + 6 + 18 + 8;
        int nvBtnX = cx + CARD_PAD;
        int nvBtnW = CARD_W - CARD_PAD * 2;
        int nvBtnH = 20;
        if (mx >= nvBtnX && mx < nvBtnX + nvBtnW && my >= y && my < y + nvBtnH) {
            Xray.setNightVisionEnabled(!Xray.isNightVisionEnabled());
            // If disabling night vision while xray is active, remove the effect
            if (!Xray.isNightVisionEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    mc.player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
                }
            }
            return true;
        }
        y += nvBtnH + 8 + 8;

        // Active blocks info + block selector button
        y += 14; // "Blocchi attivi" text
        int activeCount = Xray.getXrayBlocks().size();
        if (activeCount > 0) y += 20; // icons row

        int selBtnX = cx + CARD_PAD;
        int selBtnW = CARD_W - CARD_PAD * 2;
        int selBtnH = 22;
        if (mx >= selBtnX && mx < selBtnX + selBtnW && my >= y && my < y + selBtnH) {
            Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
            return true;
        }
        y += selBtnH + 12 + 8;

        // Back button
        int backW = 100;
        int backX = cx + (CARD_W - backW) / 2;
        int backH = 20;
        if (mx >= backX && mx < backX + backW && my >= y && my < y + backH) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double deltaX, double deltaY) {
        if (draggingSlider && button == 0) {
            updateSliderValue(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingSlider && button == 0) {
            draggingSlider = false;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    private void updateSliderValue(double mx) {
        double ratio = (mx - sliderX) / (double) sliderW;
        ratio = Math.max(0, Math.min(1, ratio));
        Xray.setOpacity((int) Math.round(ratio * 100));
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
