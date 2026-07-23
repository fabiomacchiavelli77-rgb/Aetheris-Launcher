package net.aetheris.client.gui;

import net.aetheris.client.modules.impl.render.Xray;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class XrayBlockSelectorScreen extends Screen {
    private final Screen parent;
    private final List<Block> allBlocks = new ArrayList<>();
    private EditBox searchBox;
    private int scrollOffset = 0;

    public XrayBlockSelectorScreen(Screen parent) {
        super(Component.literal("Xray Full Block Selector"));
        this.parent = parent;

        for (Block block : BuiltInRegistries.BLOCK) {
            if (block != Blocks.AIR && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                allBlocks.add(block);
            }
        }
        allBlocks.sort(Comparator.comparing(b -> b.getName().getString()));
    }

    @Override
    protected void init() {
        // Search Box (cerca sia in italiano che in inglese/ID)
        this.searchBox = new EditBox(this.font, this.width / 2 - 110, 24, 220, 18, Component.literal("Search..."));
        this.searchBox.setResponder(s -> {
            scrollOffset = 0;
            rebuildWidgets();
        });
        this.addRenderableWidget(this.searchBox);

        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.addRenderableWidget(this.searchBox);

        // 1. Tre Pulsanti di Azione Rapida in stile Aristois
        int actionBtnWidth = Math.min(100, (this.width - 20) / 3 - 5);
        int actionStartX = (this.width - (3 * (actionBtnWidth + 5) - 5)) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("§eReset Default"), b -> {
            Xray.resetDefaultBlocks();
            rebuildWidgets();
        }).bounds(actionStartX, 45, actionBtnWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("§aSeleziona Tutti"), b -> {
            Xray.selectAllBlocks(getFilteredBlocks());
            rebuildWidgets();
        }).bounds(actionStartX + actionBtnWidth + 5, 45, actionBtnWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("§cDeseleziona Tutti"), b -> {
            Xray.clearAllBlocks();
            rebuildWidgets();
        }).bounds(actionStartX + 2 * (actionBtnWidth + 5), 45, actionBtnWidth, 18).build());

        // 2. Tabella 3 Colonne per i blocchi
        List<Block> filtered = getFilteredBlocks();

        int startY = 68;
        int rowHeight = 22;
        int availableHeight = this.height - startY - 35;
        int maxRows = Math.max(1, availableHeight / rowHeight);

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + maxRows, filtered.size());

        int tableWidth = Math.min(420, this.width - 30);
        int tableX = (this.width - tableWidth) / 2;

        for (int i = startIndex; i < endIndex; i++) {
            Block block = filtered.get(i);
            boolean active = Xray.isXrayBlock(block);
            int y = startY + ((i - startIndex) * rowHeight);

            // Colonna 3: Checkbox / Toggle Button a destra
            int checkWidth = 85;
            int checkX = tableX + tableWidth - checkWidth;

            String toggleLabel = active ? "§a[✓] VISIBILE" : "§7[  ] NASCOSTO";
            this.addRenderableWidget(Button.builder(Component.literal(toggleLabel), b -> {
                Xray.toggleXrayBlock(block);
                rebuildWidgets();
            }).bounds(checkX, y, checkWidth, 19).build());
        }

        // 3. Pulsanti di Scorrimento (▲ e ▼)
        if (startIndex > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                if (scrollOffset > 0) {
                    scrollOffset--;
                    rebuildWidgets();
                }
            }).bounds(tableX - 25, startY, 20, 20).build());
        }

        if (endIndex < filtered.size()) {
            this.addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                scrollOffset++;
                rebuildWidgets();
            }).bounds(tableX - 25, startY + (maxRows - 1) * rowHeight, 20, 20).build());
        }

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 90, this.height - 28, 180, 20).build());
    }

    private List<Block> getFilteredBlocks() {
        String query = searchBox != null ? searchBox.getValue().toLowerCase().trim() : "";
        return allBlocks.stream().filter(b -> {
            if (query.isEmpty()) return true;
            String italianName = b.getName().getString().toLowerCase();
            String englishId = BuiltInRegistries.BLOCK.getKey(b).getPath().toLowerCase();
            return italianName.contains(query) || englishId.contains(query);
        }).toList();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0) {
            scrollOffset = Math.min(scrollOffset + 1, Math.max(0, getFilteredBlocks().size() - 1));
            rebuildWidgets();
            return true;
        } else if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(this.font, "§6§lXRAY FULL BLOCK SELECTOR", this.width / 2, 8, 0xFFFFFFFF);

        // Disegna la Tabella 3 Colonne per i Blocchi
        List<Block> filtered = getFilteredBlocks();

        int startY = 68;
        int rowHeight = 22;
        int availableHeight = this.height - startY - 35;
        int maxRows = Math.max(1, availableHeight / rowHeight);

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + maxRows, filtered.size());

        int tableWidth = Math.min(420, this.width - 30);
        int tableX = (this.width - tableWidth) / 2;

        for (int i = startIndex; i < endIndex; i++) {
            Block block = filtered.get(i);
            int y = startY + ((i - startIndex) * rowHeight);

            // Sfondo riga della tabella
            int bgColor = (i % 2 == 0) ? 0x66000000 : 0x44000000;
            g.fill(tableX, y, tableX + tableWidth - 90, y + 19, bgColor);

            // Colonna 1: Icona 3D dell'oggetto Minecraft (x + 2, y + 1)
            g.renderFakeItem(new ItemStack(block), tableX + 3, y + 1);

            // Colonna 2: Doppio Nome (Italiano + Inglese/ID)
            String italianName = block.getName().getString();
            String englishId = BuiltInRegistries.BLOCK.getKey(block).getPath();
            String label = italianName + " §7(" + englishId + ")";

            if (label.length() > 38) label = label.substring(0, 36) + "..";

            g.drawString(this.font, label, tableX + 24, y + 5, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
