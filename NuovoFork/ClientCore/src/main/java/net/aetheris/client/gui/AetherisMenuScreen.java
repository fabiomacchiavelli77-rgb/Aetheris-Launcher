package net.aetheris.client.gui;

import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import net.aetheris.client.modules.Category;

import java.util.List;

public class AetherisMenuScreen extends Screen {
    private Category currentCategory = Category.COMBAT;
    private int scrollOffset = 0;
    private static final int MODULES_PER_PAGE = 12;

    public AetherisMenuScreen() {
        super(Component.literal("Aetheris Menu"));
    }

    @Override
    protected void init() {
        this.clearWidgets();
        scrollOffset = 0;

        int btnWidth = 90;
        Category[] cats = Category.values();
        int totalWidth = cats.length * (btnWidth + 5) - 5;
        int startX = (this.width - totalWidth) / 2;

        for (Category cat : cats) {
            this.addRenderableWidget(Button.builder(
                Component.literal(cat.getName()),
                button -> {
                    this.currentCategory = cat;
                    this.scrollOffset = 0;
                    this.rebuildWidgets();
                }
            ).bounds(startX, 30, btnWidth, 20).build());
            startX += btnWidth + 5;
        }

        rebuildWidgets();
    }

    private void rebuildWidgets() {
        // Remove module buttons only, keep category buttons
        this.children().removeIf(w -> !(w instanceof Button b && b.getMessage().getString().equals(
            java.util.Arrays.stream(Category.values())
                .map(c -> c.getName())
                .toList()
                .contains(b.getMessage().getString())
        )));

        List<Module> catModules = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory)
            .toList();

        int startY = 65;
        int endIdx = Math.min(scrollOffset + MODULES_PER_PAGE, catModules.size());

        for (int i = scrollOffset; i < endIdx; i++) {
            Module mod = catModules.get(i);
            int y = startY + ((i - scrollOffset) * 25);

            String label = mod.getName() + ": " + (mod.isEnabled() ? "§aON" : "§cOFF");
            this.addRenderableWidget(Button.builder(
                Component.literal(label),
                button -> {
                    mod.toggle();
                    button.setMessage(Component.literal(
                        mod.getName() + ": " + (mod.isEnabled() ? "§aON" : "§cOFF")
                    ));
                }
            ).bounds(this.width / 2 - 110, y, 220, 20).build());
        }

        // Scroll buttons
        if (catModules.size() > MODULES_PER_PAGE) {
            if (scrollOffset > 0) {
                this.addRenderableWidget(Button.builder(
                    Component.literal("▲"), b -> { scrollOffset--; this.rebuildWidgets(); }
                ).bounds(this.width / 2 - 120, 55, 20, 12).build());
            }
            if (scrollOffset + MODULES_PER_PAGE < catModules.size()) {
                this.addRenderableWidget(Button.builder(
                    Component.literal("▼"), b -> { scrollOffset++; this.rebuildWidgets(); }
                ).bounds(this.width / 2 + 100, 55, 20, 12).build());
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(
            this.font,
            "AETHERIS - " + currentCategory.getName().toUpperCase(),
            this.width / 2, 10, 0xFFaa00aa
        );
        long count = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory).count();
        guiGraphics.drawCenteredString(
            this.font,
            "Moduli: " + count,
            this.width / 2, 22, 0xFF888888
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
