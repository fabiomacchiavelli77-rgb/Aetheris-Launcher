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
    private static final int MODULES_PER_PAGE = 10;
    private Module recordingKeybind = null;

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
                button -> { currentCategory = cat; scrollOffset = 0; rebuildWidgets(); }
            ).bounds(startX, 30, btnWidth, 20).build());
            startX += btnWidth + 5;
        }
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        this.children().clear();
        // Ricrea bottoni categoria
        int btnWidth = 90;
        Category[] cats = Category.values();
        int totalWidth = cats.length * (btnWidth + 5) - 5;
        int startX = (this.width - totalWidth) / 2;
        for (Category cat : cats) {
            this.addRenderableWidget(Button.builder(
                Component.literal(cat.getName()),
                button -> { currentCategory = cat; scrollOffset = 0; rebuildWidgets(); }
            ).bounds(startX, 30, btnWidth, 20).build());
            startX += btnWidth + 5;
        }

        List<Module> catModules = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory).toList();

        int startY = 65;
        int endIdx = Math.min(scrollOffset + MODULES_PER_PAGE, catModules.size());

        for (int i = scrollOffset; i < endIdx; i++) {
            Module mod = catModules.get(i);
            int y = startY + ((i - scrollOffset) * 25);

            String kb = mod.getKeybindName() == null ? "None" : mod.getKeybindName();
            if (recordingKeybind == mod) kb = "...";

            String label = mod.getName() + " [" + kb + "]: " + (mod.isEnabled() ? "§aON" : "§cOFF");
            this.addRenderableWidget(Button.builder(
                Component.literal(label),
                button -> {
                    if (hasShiftDown()) {
                        // Shift+click = registra keybind
                        recordingKeybind = mod;
                        rebuildWidgets();
                    } else {
                        mod.toggle();
                        button.setMessage(Component.literal(
                            mod.getName() + " [" + (mod.getKeybindName() == null ? "None" : mod.getKeybindName()) + "]: "
                            + (mod.isEnabled() ? "§aON" : "§cOFF")
                        ));
                    }
                }
            ).bounds(this.width / 2 - 120, y, 240, 20).build());
        }

        // Scroll buttons
        if (catModules.size() > MODULES_PER_PAGE) {
            if (scrollOffset > 0)
                this.addRenderableWidget(Button.builder(Component.literal("▲"),
                    b -> { scrollOffset--; rebuildWidgets(); }).bounds(this.width / 2 - 125, 55, 20, 12).build());
            if (scrollOffset + MODULES_PER_PAGE < catModules.size())
                this.addRenderableWidget(Button.builder(Component.literal("▼"),
                    b -> { scrollOffset++; rebuildWidgets(); }).bounds(this.width / 2 + 105, 55, 20, 12).build());
        }
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (recordingKeybind != null) {
            if (key == 256) { // ESC = unbind
                recordingKeybind.setKeybind(-1);
            } else {
                recordingKeybind.setKeybind(key);
            }
            recordingKeybind = null;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(this.font,
            "AETHERIS - " + currentCategory.getName().toUpperCase(),
            this.width / 2, 10, 0xFFaa00aa);
        long count = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory).count();
        guiGraphics.drawCenteredString(this.font,
            "Moduli: " + count + " | Shift+Click = Keybind | Esc = Unbind",
            this.width / 2, 22, 0xFF888888);
        if (recordingKeybind != null) {
            guiGraphics.drawCenteredString(this.font,
                "Premi un tasto per " + recordingKeybind.getName() + " (ESC = unbind)",
                this.width / 2, this.height - 15, 0xFFFFFF00);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
