package net.aetheris.client.gui;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AetherisMenuScreen extends Screen {
    private Category currentCategory = Category.COMBAT;
    private int scrollOffset = 0;
    private Module recordingKeybind = null;

    public AetherisMenuScreen() {
        super(Component.literal("Aetheris Menu"));
    }

    @Override
    protected void init() {
        scrollOffset = 0;
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        // 1. Bottoni categoria in alto (calcolo proporzionale alla larghezza)
        Category[] cats = Category.values();
        int btnWidth = Math.min(90, Math.max(55, (this.width - 20) / cats.length - 4));
        int totalWidth = cats.length * (btnWidth + 4) - 4;
        int startX = (this.width - totalWidth) / 2;

        for (Category cat : cats) {
            String label = (cat == currentCategory ? "§a> " : "") + cat.getName();
            this.addRenderableWidget(Button.builder(
                Component.literal(label),
                button -> { currentCategory = cat; scrollOffset = 0; rebuildWidgets(); }
            ).bounds(startX, 30, btnWidth, 20).build());
            startX += btnWidth + 4;
        }

        // 2. Calcolo dinamico dello spazio verticale disponibile
        int startY = 58;
        int navY = this.height - 28;
        int availableHeight = navY - startY - 10;
        int modulesPerPage = Math.max(1, availableHeight / 25);

        List<Module> catModules = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory).toList();

        int endIdx = Math.min(scrollOffset + modulesPerPage, catModules.size());

        // 3. Bottoni moduli
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
                        recordingKeybind = mod;
                        rebuildWidgets();
                    } else {
                        mod.toggle();
                        rebuildWidgets();
                    }
                }
            ).bounds(this.width / 2 - 120, y, 240, 20).build());
        }

        // 4. Bottoni di scorrimento (se i moduli superano la capienza della pagina)
        if (catModules.size() > modulesPerPage) {
            if (scrollOffset > 0)
                this.addRenderableWidget(Button.builder(Component.literal("▲"),
                    b -> { scrollOffset--; rebuildWidgets(); }).bounds(this.width / 2 - 145, startY, 20, 20).build());
            if (scrollOffset + modulesPerPage < catModules.size())
                this.addRenderableWidget(Button.builder(Component.literal("▼"),
                    b -> { scrollOffset++; rebuildWidgets(); }).bounds(this.width / 2 + 125, startY, 20, 20).build());
        }

        // 5. Bottom Navigation Bar ancorata in basso
        int bWidth = Math.min(100, (this.width - 20) / 4 - 5);
        int bStartX = (this.width - (4 * (bWidth + 5) - 5)) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("§bKeybinds"), b -> {
            this.minecraft.setScreen(new KeybindManagerScreen(this));
        }).bounds(bStartX, navY, bWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§6Xray Ores"), b -> {
            this.minecraft.setScreen(new XrayBlockSelectorScreen(this));
        }).bounds(bStartX + bWidth + 5, navY, bWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§dAlt Manager"), b -> {
            this.minecraft.setScreen(new AltManagerScreen(this));
        }).bounds(bStartX + 2 * (bWidth + 5), navY, bWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("§eSeedCracker"), b -> {
            this.minecraft.setScreen(new SeedCrackerConfigScreen(this));
        }).bounds(bStartX + 3 * (bWidth + 5), navY, bWidth, 20).build());
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (recordingKeybind != null) {
            if (key == 256) {
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
            this.width / 2, 8, 0xFFaa00aa);
        long count = ModuleManager.getModules().stream()
            .filter(m -> m.getCategory() == currentCategory).count();
        guiGraphics.drawCenteredString(this.font,
            "Moduli: " + count + " | Shift+Click = Keybind | Esc = Unbind",
            this.width / 2, 19, 0xFF888888);
        if (recordingKeybind != null) {
            guiGraphics.drawCenteredString(this.font,
                "Premi un tasto per " + recordingKeybind.getName() + " (ESC = unbind)",
                this.width / 2, this.height - 42, 0xFFFFFF00);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
