package net.aetheris.client.gui;

import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class KeybindManagerScreen extends Screen {
    private final Screen parent;
    private EditBox searchBox;
    private Module listeningModule = null;

    public KeybindManagerScreen(Screen parent) {
        super(Component.literal("Keybind Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 28, 200, 18, Component.literal("Search..."));
        this.searchBox.setResponder(s -> rebuildWidgets());
        this.addRenderableWidget(this.searchBox);

        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.addRenderableWidget(this.searchBox);

        String query = searchBox != null ? searchBox.getValue().toLowerCase().trim() : "";

        List<Module> filteredMods = ModuleManager.getModules().stream()
            .filter(m -> query.isEmpty() || m.getName().toLowerCase().contains(query) || m.getCategory().getName().toLowerCase().contains(query))
            .toList();

        int startY = 52;
        int y = startY;
        int btnWidth = Math.min(280, this.width - 40);

        for (Module mod : filteredMods) {
            if (y + 20 > this.height - 40) break;

            String kbName = (listeningModule == mod) ? "§e[Press Key...]" : ("§b[" + (mod.getKeybindName() == null ? "None" : mod.getKeybindName()) + "]");
            String label = mod.getName() + " (" + mod.getCategory().getName() + ") -> " + kbName;

            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                listeningModule = mod;
                rebuildWidgets();
            }).bounds(this.width / 2 - btnWidth / 2, y, btnWidth, 20).build());

            y += 23;
        }

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (listeningModule != null) {
            if (key == 256) {
                listeningModule.setKeybind(-1);
            } else {
                listeningModule.setKeybind(key);
            }
            listeningModule = null;
            rebuildWidgets();
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, "§b§lKEYBIND MANAGER", this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
