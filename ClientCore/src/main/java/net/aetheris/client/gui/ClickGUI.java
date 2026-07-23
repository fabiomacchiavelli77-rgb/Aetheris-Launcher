package net.aetheris.client.gui;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClickGUI extends Screen {
    private final List<Panel> panels = new ArrayList<>();
    private Panel dragging = null;
    private int dragX, dragY;

    private static final int PANEL_WIDTH = 120;
    private static final int TITLE_HEIGHT = 16;
    private static final int MODULE_HEIGHT = 13;

    public ClickGUI() {
        super(Component.literal("Aetheris ClickGUI"));

        int x = 10;
        int y = 10;
        for (Category cat : Category.values()) {
            Panel panel = new Panel(cat, x, y);
            panels.add(panel);
            y += panel.getHeight();
            if (y > 300) { y = 10; x += PANEL_WIDTH + 10; }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Sfondo semi-trasparente
        g.fill(0, 0, width, height, 0x80000000);

        for (Panel panel : panels) {
            panel.render(g, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel panel = panels.get(i);
            if (panel.titleBounds.contains(mx, my) && button == 0) {
                dragging = panel;
                dragX = (int) (mx - panel.x);
                dragY = (int) (my - panel.y);
                return true;
            }
            if (panel.isInside(mx, my)) {
                panel.click(mx, my);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            dragging.x = (int) (mx - dragX);
            dragging.y = (int) (my - dragY);
            dragging.x = Math.max(0, Math.min(dragging.x, this.width - PANEL_WIDTH));
            dragging.y = Math.max(0, Math.min(dragging.y, this.height - TITLE_HEIGHT));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // === Inner Panel class ===

    private static class Panel {
        final Category category;
        int x, y;
        boolean expanded = true;
        final Rect titleBounds = new Rect();
        final List<ModButton> buttons = new ArrayList<>();

        Panel(Category cat, int x, int y) {
            this.category = cat;
            this.x = x;
            this.y = y;
            rebuildButtons();
        }

        void rebuildButtons() {
            buttons.clear();
            List<Module> mods = ModuleManager.getModules(category);
            for (int i = 0; i < mods.size(); i++) {
                buttons.add(new ModButton(mods.get(i), i));
            }
        }

        int getHeight() {
            if (!expanded) return TITLE_HEIGHT;
            return TITLE_HEIGHT + buttons.size() * MODULE_HEIGHT + 2;
        }

        int getColor() {
            return switch (category) {
                case COMBAT -> 0xFFFF4444;
                case MOVEMENT -> 0xFF44FF44;
                case RENDER -> 0xFF44AAFF;
                case WORLD -> 0xFFFFAA00;
                case PLAYER -> 0xFFFF44FF;
                case SEEDCRACKER -> 0xFF44FFAA;
            };
        }

        boolean isInside(double mx, double my) {
            return mx >= x && mx <= x + PANEL_WIDTH && my >= y && my <= y + getHeight();
        }

        void click(double mx, double my) {
            if (titleBounds.contains(mx, my)) {
                expanded = !expanded;
                rebuildButtons();
                return;
            }
            if (!expanded) return;
            for (ModButton btn : buttons) {
                if (btn.bounds.contains(mx, my)) {
                    btn.module.toggle();
                    ProfileManager.getInstance().onModuleChanged();
                    return;
                }
            }
        }

        void render(GuiGraphics g, int mouseX, int mouseY) {
            int color = getColor();
            int panelHeight = getHeight();

            // Bordo
            g.fill(x - 1, y - 1, x + PANEL_WIDTH + 1, y + panelHeight + 1, 0xFF000000);
            // Sfondo
            g.fill(x, y, x + PANEL_WIDTH, y + panelHeight, 0xCC1A1A2E);

            // Titolo
            titleBounds.set(x, y, PANEL_WIDTH, TITLE_HEIGHT);
            g.fill(x, y, x + PANEL_WIDTH, y + TITLE_HEIGHT, color);
            g.drawCenteredString(Minecraft.getInstance().font,
                category.getName() + (expanded ? " ▼" : " ▶"),
                x + PANEL_WIDTH / 2, y + 4,
                0xFFFFFFFF);

            if (!expanded) return;

            // Bottoni moduli
            int by = y + TITLE_HEIGHT + 1;
            for (ModButton btn : buttons) {
                btn.bounds.set(x + 1, by, PANEL_WIDTH - 2, MODULE_HEIGHT);
                boolean hover = btn.bounds.contains(mouseX, mouseY);

                int bgColor = btn.module.isEnabled() ? color : 0xFF333355;
                if (hover) bgColor = brighten(bgColor);

                g.fill(btn.bounds.x, btn.bounds.y,
                    btn.bounds.x + btn.bounds.w, btn.bounds.y + btn.bounds.h, bgColor);

                String label = btn.module.getName();
                g.drawString(Minecraft.getInstance().font, label,
                    btn.bounds.x + 4, btn.bounds.y + 2, 0xFFFFFFFF);

                by += MODULE_HEIGHT;
            }
        }

        private int brighten(int c) {
            int r = Math.min(255, ((c >> 16) & 0xFF) + 40);
            int g = Math.min(255, ((c >> 8) & 0xFF) + 40);
            int b = Math.min(255, (c & 0xFF) + 40);
            return (c & 0xFF000000) | (r << 16) | (g << 8) | b;
        }
    }

    private static class ModButton {
        final Module module;
        final Rect bounds = new Rect();

        ModButton(Module mod, int index) {
            this.module = mod;
        }
    }

    private static class Rect {
        int x, y, w, h;
        void set(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
