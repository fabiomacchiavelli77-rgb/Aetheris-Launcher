package net.aetheris.client.gui;

import kaptainwutax.seedcrackerX.SeedCracker;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.finder.Finder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Set;

public class SeedCrackerConfigScreen extends Screen {
    private final Screen parent;
    private final Config config = Config.get();
    private Tab currentTab = Tab.GENERAL;

    public enum Tab {
        GENERAL("General"),
        STRUCTURES("Structures"),
        DECORATORS("Decorators"),
        BIOMES("Biomes"),
        SEEDS("Cracked Seeds");

        private final String name;
        Tab(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public SeedCrackerConfigScreen(Screen parent) {
        super(Component.literal("SeedCrackerX Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();

        // 1. Schede/Tab in alto
        Tab[] tabs = Tab.values();
        int tabWidth = Math.min(90, (this.width - 20) / tabs.length - 4);
        int totalTabWidth = tabs.length * (tabWidth + 4) - 4;
        int startX = (this.width - totalTabWidth) / 2;

        for (Tab tab : tabs) {
            String label = (tab == currentTab ? "§e> " : "") + tab.getName();
            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                currentTab = tab;
                rebuildWidgets();
            }).bounds(startX, 30, tabWidth, 20).build());
            startX += tabWidth + 4;
        }

        // 2. Contenuto della scheda corrente
        int startY = 60;
        int btnWidth = 260;
        int y = startY;

        switch (currentTab) {
            case GENERAL -> {
                String activeLabel = "SeedCracker Finder: " + (config.active ? "§aACTIVE" : "§cDISABLED");
                this.addRenderableWidget(Button.builder(Component.literal(activeLabel), b -> {
                    config.active = !config.active;
                    Config.save();
                    rebuildWidgets();
                }).bounds(this.width / 2 - btnWidth / 2, y, btnWidth, 20).build());

                String antiXrayLabel = "Anti-Xray Mode: " + (config.antiXrayBypass ? "§aON" : "§cOFF");
                this.addRenderableWidget(Button.builder(Component.literal(antiXrayLabel), b -> {
                    config.antiXrayBypass = !config.antiXrayBypass;
                    Config.save();
                    rebuildWidgets();
                }).bounds(this.width / 2 - btnWidth / 2, y + 25, btnWidth, 20).build());

                String dbLabel = "Database Submits: " + (config.databaseSubmits ? "§aON" : "§cOFF");
                this.addRenderableWidget(Button.builder(Component.literal(dbLabel), b -> {
                    config.databaseSubmits = !config.databaseSubmits;
                    Config.save();
                    rebuildWidgets();
                }).bounds(this.width / 2 - btnWidth / 2, y + 50, btnWidth, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("§cReset Collected Data"), b -> {
                    SeedCracker.get().reset();
                    rebuildWidgets();
                }).bounds(this.width / 2 - btnWidth / 2, y + 75, btnWidth, 20).build());
            }

            case STRUCTURES -> renderFinderList(Finder.Category.STRUCTURES, startY, btnWidth);
            case DECORATORS -> renderFinderList(Finder.Category.DECORATORS, startY, btnWidth);
            case BIOMES -> renderFinderList(Finder.Category.BIOMES, startY, btnWidth);

            case SEEDS -> {
                // Scheda semi trovati
            }
        }

        // Back button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build());
    }

    private void renderFinderList(Finder.Category category, int startY, int btnWidth) {
        List<Finder.Type> finders = Finder.Type.getForCategory(category);
        int y = startY;

        for (Finder.Type finder : finders) {
            boolean active = finder.enabled.get();
            String name = finder.name().replace("_", " ");
            String label = name + ": " + (active ? "§aENABLED" : "§cDISABLED");

            this.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                finder.enabled.set(!active);
                Config.save();
                rebuildWidgets();
            }).bounds(this.width / 2 - btnWidth / 2, y, btnWidth, 18).build());

            y += 22;
            if (y > this.height - 55) break;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, "§e§lSEEDCRACKER X CONFIGURATION", this.width / 2, 10, 0xFFFFFFFF);

        if (currentTab == Tab.SEEDS) {
            Set<Long> worldSeeds = SeedCracker.get().getDataStorage().getTimeMachine().worldSeeds;
            Set<Long> structureSeeds = SeedCracker.get().getDataStorage().getTimeMachine().structureSeeds;

            int y = 70;
            g.drawCenteredString(this.font, "§aWorld Seeds Cracked: " + (worldSeeds.isEmpty() ? "None" : worldSeeds.toString()), this.width / 2, y, 0xFFFFFFFF);
            g.drawCenteredString(this.font, "§bStructure Seeds Cracked: " + (structureSeeds.isEmpty() ? "None" : structureSeeds.toString()), this.width / 2, y + 25, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
