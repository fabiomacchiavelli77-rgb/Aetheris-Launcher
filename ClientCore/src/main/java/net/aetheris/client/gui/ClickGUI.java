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

import java.util.ArrayList;
import java.util.List;

/**
 * Premium ClickGUI inspired by Aristois, Wurst, and Meteor Client.
 *
 * Features:
 * - Horizontal category columns with draggable support
 * - Rounded-style module cards with smooth hover transitions
 * - Inline expanding settings drawer with keybind and config buttons
 * - Real-time search bar with animated cursor
 * - Per-category accent colors with header glow effect
 * - Scrollable columns when modules exceed screen height
 */
public class ClickGUI extends Screen {

    // ── layout constants ───────────────────────────────────────────────
    private static final int HEADER_H    = ClickGUILayout.HEADER_HEIGHT;
    private static final int ROW_H       = ClickGUILayout.ROW_HEIGHT;
    private static final int SETTINGS_H  = 20;     // expanded settings drawer height
    private static final int SEARCH_W    = 140;
    private static final int SEARCH_H    = 18;
    private static final int SCROLL_BTN  = 10;     // scroll indicator size

    // ── state ──────────────────────────────────────────────────────────
    private final List<Column> columns = new ArrayList<>();
    private Module bindingModule = null;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private long openTime = 0;

    public ClickGUI() {
        super(Component.literal("Aetheris ClickGUI"));
        for (Category cat : Category.values()) {
            columns.add(new Column(cat));
        }
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();
        applyLayoutAndClamp();
    }

    // ── accent colours (Aristois-faithful palette) ─────────────────────
    private static int accentOf(Category c) {
        return switch (c) {
            case COMBAT      -> 0xFFE05555;   // crimson red
            case MOVEMENT    -> 0xFF55CC55;   // emerald green
            case RENDER      -> 0xFF55AADD;   // sky blue
            case WORLD       -> 0xFFDDAA44;   // amber gold
            case PLAYER      -> 0xFF9966DD;   // violet
            case SEEDCRACKER -> 0xFF44DDAA;   // teal
        };
    }

    /** Darker variant of accent for backgrounds */
    private static int accentDark(Category c) {
        int a = accentOf(c);
        int r = ((a >> 16) & 0xFF) / 3;
        int g = ((a >> 8) & 0xFF) / 3;
        int b = (a & 0xFF) / 3;
        return 0xD0000000 | (r << 16) | (g << 8) | b;
    }

    // ── layout helpers ─────────────────────────────────────────────────

    private ClickGUILayout.Layout layout() {
        return ClickGUILayout.calculate(width, height, columns.size());
    }

    private void applyLayoutAndClamp() {
        ClickGUILayout.Layout layout = layout();
        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);
            column.x = layout.columnX(index);
            column.y = layout.columnTopY();
            java.util.List<Module> modules = column.filteredModules(searchQuery);
            column.scrollOffset = layout.clampScrollOffset(column.scrollOffset, modules.size());
            if (column.expandedModule != null && !modules.contains(column.expandedModule)) {
                column.expandedModule = null;
            }
        }
    }


        private void setSearchQuery(String query) {
        searchQuery = query;
        applyLayoutAndClamp();
    }

    private enum UtilityAction {
        KEYBINDS("Keybinds"), XRAY("Xray"), ALTS("Alts"), SEED("Seed");

        private final String label;
        UtilityAction(String label) { this.label = label; }
    }

    private void openUtility(UtilityAction action) {
        switch (action) {
            case KEYBINDS -> Minecraft.getInstance().setScreen(new KeybindManagerScreen(this));
            case XRAY -> Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
            case ALTS -> Minecraft.getInstance().setScreen(new AltManagerScreen(this));
            case SEED -> Minecraft.getInstance().setScreen(new SeedCrackerConfigScreen(this));
        }
    }

    // ── rendering ──────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // Smooth fade-in
        long elapsed = System.currentTimeMillis() - openTime;
        int bgAlpha = (int) Math.min(0xB0, elapsed * 0.8);

        // Full-screen dark overlay
        g.fill(0, 0, width, height, (bgAlpha << 24) | 0x0C0C14);

        // ── Render each column ──
        for (Column col : columns) {
            renderColumn(g, col, mouseX, mouseY);
        }

        // ── Search bar (bottom center, sleek) ──
        renderSearchBar(g, mouseX, mouseY);

        // ── Binding notice ──
        if (bindingModule != null) {
            String notice = "§e⌨ Press key for §f" + bindingModule.getName() + " §7(ESC = clear)";
            g.drawCenteredString(font, notice, width / 2, height - 28, 0xFFFFFF00);
        }

        // ── Watermark ──
        g.drawString(font, "§7Aetheris §8v1.0", 4, height - 12, 0xFF505050);

        // ── Aetheris Header and Dock ──
        int activeCount = 0;
        for (Module m : ModuleManager.getModules()) if (m.isEnabled()) activeCount++;
        String headerText = "AETHERIS §8[" + activeCount + "]";
        g.drawCenteredString(font, headerText, width / 2, 10, 0xFFFFFFFF);

        int dockW = 200;
        int dockH = 16;
        int dockX = (width - dockW) / 2;
        int dockY = height - SEARCH_H - 10 - dockH - 4; // Above search bar
        
        int btnW = (dockW - 12) / 4;
        UtilityAction[] actions = UtilityAction.values();
        for (int i = 0; i < actions.length; i++) {
            UtilityAction action = actions[i];
            int bx = dockX + i * (btnW + 4);
            boolean hover = mouseX >= bx && mouseX < bx + btnW && mouseY >= dockY && mouseY < dockY + dockH;
            g.fill(bx, dockY, bx + btnW, dockY + dockH, hover ? 0xFF3A4050 : 0xFF2A3040);
            g.drawCenteredString(font, action.label, bx + btnW / 2, dockY + 4, 0xFFCCCCCC);
        }
    }

    private void renderColumn(GuiGraphics g, Column col, int mouseX, int mouseY) {
        int cx = col.x;
        int cy = col.y;
        int accent = accentOf(col.category);
        List<Module> mods = col.filteredModules(searchQuery);
        int maxRows = layout().maxVisibleRows();
        boolean needsScroll = mods.size() > maxRows;
        int visibleCount = needsScroll ? maxRows : mods.size();
        int startIdx = col.scrollOffset;
        int endIdx = Math.min(startIdx + maxRows, mods.size());
        int expandedExtra = 0;
        if (col.expandedModule != null && mods.contains(col.expandedModule)) {
            int eIdx = mods.indexOf(col.expandedModule);
            if (eIdx >= startIdx && eIdx < endIdx) expandedExtra = SETTINGS_H;
        }
        int totalH = HEADER_H + visibleCount * ROW_H + expandedExtra;

        // ── Column shadow (subtle depth) ──
        g.fill(cx + 2, cy + 2, cx + layout().columnWidth() + 2, cy + totalH + 2, 0x40000000);

        // ── Column background ──
        g.fill(cx, cy, cx + layout().columnWidth(), cy + totalH, 0xE8141820);

        // ── Header bar ──
        g.fill(cx, cy, cx + layout().columnWidth(), cy + HEADER_H, 0xF0181E28);
        // Top accent line (3px gradient glow)
        g.fill(cx, cy, cx + layout().columnWidth(), cy + 1, accent);
        g.fill(cx, cy + 1, cx + layout().columnWidth(), cy + 2, (accent & 0x00FFFFFF) | 0x80000000);
        // Category name centered in header
        g.drawCenteredString(font, col.category.getName(), cx + layout().columnWidth() / 2, cy + 7, 0xFFFFFFFF);
        // Module count badge (right side)
        String countBadge = "§8[" + mods.size() + "]";
        g.drawString(font, countBadge, cx + layout().columnWidth() - font.width(countBadge) - 3, cy + 7, 0xFF606060);

        // ── Module rows ──
        int ry = cy + HEADER_H;

        // Scroll up indicator
        if (col.scrollOffset > 0) {
            g.drawCenteredString(font, "§7▲", cx + layout().columnWidth() / 2, ry - 1, 0xFF808080);
        }

        for (int i = startIdx; i < endIdx; i++) {
            Module mod = mods.get(i);
            boolean enabled = mod.isEnabled();
            boolean hover = mouseX >= cx && mouseX <= cx + layout().columnWidth()
                         && mouseY >= ry && mouseY < ry + ROW_H;

            // Row background — Wurst/Meteor style
            int bg;
            if (enabled && hover) {
                // Enabled + hover: brighter accent
                bg = (accent & 0x00FFFFFF) | 0x70000000;
            } else if (enabled) {
                // Enabled: accent tinted
                bg = (accent & 0x00FFFFFF) | 0x50000000;
            } else if (hover) {
                // Hover only: subtle white overlay
                bg = 0x28FFFFFF;
            } else {
                // Default: alternate rows for readability
                bg = (i % 2 == 0) ? 0x08FFFFFF : 0x00000000;
            }
            g.fill(cx, ry, cx + layout().columnWidth(), ry + ROW_H, bg);

            // Left accent bar when enabled (LiquidBounce style)
            if (enabled) {
                g.fill(cx, ry, cx + 2, ry + ROW_H, accent);
            }

            // Module name
            int textCol = enabled ? 0xFFFFFFFF : (hover ? 0xFFDDDDDD : 0xFFAAAAAA);
            // Fix: cap at 0xFF for each channel
            if (!enabled && !hover) textCol = 0xFFAAAAAA;
            g.drawString(font, mod.getName(), cx + 6, ry + 4, textCol);

            // Keybind label (right-aligned, dimmed)
            if (mod.getKeybind() != GLFW.GLFW_KEY_UNKNOWN) {
                String kn = keyName(mod.getKeybind());
                int tw = font.width(kn);
                g.drawString(font, kn, cx + layout().columnWidth() - tw - 5, ry + 4, 0xFF555555);
            }

            // Small expand arrow if right-clickable
            String arrow = (col.expandedModule == mod) ? "▾" : "▸";
            g.drawString(font, arrow, cx + layout().columnWidth() - 10, ry + 4, 0xFF666666);

            ry += ROW_H;

            // ── Expanded settings drawer (inline, Meteor-inspired) ──
            if (col.expandedModule == mod) {
                renderSettingsDrawer(g, cx, ry, mod, mouseX, mouseY);
                ry += SETTINGS_H;
            }
        }

        // Scroll down indicator
        if (endIdx < mods.size()) {
            g.drawCenteredString(font, "§7▼", cx + layout().columnWidth() / 2, ry + 1, 0xFF808080);
        }

        // Bottom border accent line
        g.fill(cx, cy + totalH - 1, cx + layout().columnWidth(), cy + totalH, (accent & 0x00FFFFFF) | 0x30000000);
    }

    private void renderSettingsDrawer(GuiGraphics g, int cx, int ry, Module mod, int mouseX, int mouseY) {
        // Drawer background — slightly indented, darker
        g.fill(cx + 3, ry, cx + layout().columnWidth() - 3, ry + SETTINGS_H, 0xE8101420);
        g.fill(cx + 3, ry, cx + layout().columnWidth() - 3, ry + 1, 0x40FFFFFF);   // top separator line

        int halfW = (layout().columnWidth() - 6) / 2;
        int btnY = ry + 2;
        int btnH = SETTINGS_H - 4;

        // ── Keybind button ──
        boolean isBinding = (bindingModule == mod);
        boolean hoverBind = mouseX >= cx + 3 && mouseX <= cx + 3 + halfW
                         && mouseY >= btnY && mouseY < btnY + btnH;
        int bindBg = isBinding ? 0xFF8B6914 : (hoverBind ? 0xFF2A3040 : 0xFF1C2030);
        g.fill(cx + 3, btnY, cx + 3 + halfW, btnY + btnH, bindBg);

        String bindLabel = isBinding ? "§e[...]" : "§7[§f" + keyName(mod.getKeybind()) + "§7]";
        g.drawCenteredString(font, bindLabel, cx + 3 + halfW / 2, btnY + 3, 0xFFFFFFFF);

        // ── Settings / config button ──
        boolean hoverCfg = mouseX >= cx + 3 + halfW + 2 && mouseX <= cx + layout().columnWidth() - 3
                        && mouseY >= btnY && mouseY < btnY + btnH;
        int cfgBg = hoverCfg ? 0xFF2A3040 : 0xFF1C2030;
        g.fill(cx + 3 + halfW + 2, btnY, cx + layout().columnWidth() - 3, btnY + btnH, cfgBg);

        String cfgLabel = settingsLabelFor(mod);
        g.drawCenteredString(font, cfgLabel, cx + 3 + halfW + 2 + (halfW - 2) / 2, btnY + 3, 0xFFDDDDDD);
    }

    private void renderSearchBar(GuiGraphics g, int mouseX, int mouseY) {
        int sbx = (width - SEARCH_W) / 2;
        int sby = height - SEARCH_H - 10;

        boolean hover = mouseX >= sbx && mouseX <= sbx + SEARCH_W
                     && mouseY >= sby && mouseY <= sby + SEARCH_H;

        // Shadow
        g.fill(sbx + 1, sby + 1, sbx + SEARCH_W + 1, sby + SEARCH_H + 1, 0x40000000);
        // Border
        int borderCol = searchFocused ? 0xFF556688 : (hover ? 0xFF3A4050 : 0xFF2A3040);
        g.fill(sbx - 1, sby - 1, sbx + SEARCH_W + 1, sby + SEARCH_H + 1, borderCol);
        // Background
        g.fill(sbx, sby, sbx + SEARCH_W, sby + SEARCH_H, searchFocused ? 0xFF1A2030 : 0xFF141820);

        // Search icon
        g.drawString(font, "§7🔍", sbx + 3, sby + 4, 0xFF808080);

        // Text
        String display;
        if (searchQuery.isEmpty() && !searchFocused) {
            display = "§8Search modules...";
        } else {
            display = searchQuery + (searchFocused ? "§f_" : "");
        }
        g.drawString(font, display, sbx + 16, sby + 5, 0xFFCCCCCC);
    }

    // ── mouse handling ─────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Dismiss binding on stray click
        if (bindingModule != null && button == 0) {
            bindingModule = null;
            return true;
        }

        // Utility dock
        int dockW = 200;
        int dockH = 16;
        int dockX = (width - dockW) / 2;
        int dockY = height - SEARCH_H - 10 - dockH - 4;
        if (my >= dockY && my < dockY + dockH && button == 0) {
            int btnW = (dockW - 12) / 4;
            for (int i = 0; i < 4; i++) {
                int bx = dockX + i * (btnW + 4);
                if (mx >= bx && mx < bx + btnW) {
                    openUtility(UtilityAction.values()[i]);
                    return true;
                }
            }
        }

        // Search bar
        int sbx = (width - SEARCH_W) / 2;
        int sby = height - SEARCH_H - 10;
        if (mx >= sbx && mx <= sbx + SEARCH_W && my >= sby && my <= sby + SEARCH_H) {
            searchFocused = true;
            return true;
        } else {
            searchFocused = false;
        }

        // Columns
        for (Column col : columns) {
            int cx = col.x;
            int cy = col.y;
            List<Module> mods = col.filteredModules(searchQuery);
            int maxRows = layout().maxVisibleRows();
            int startIdx = col.scrollOffset;
            int endIdx = Math.min(startIdx + maxRows, mods.size());

            int ry = cy + HEADER_H;
            for (int i = startIdx; i < endIdx; i++) {
                Module mod = mods.get(i);

                // Module row click
                if (mx >= cx && mx <= cx + layout().columnWidth() && my >= ry && my < ry + ROW_H) {
                    if (button == 0) {
                        mod.toggle();
                        ProfileManager.getInstance().onModuleChanged();
                    } else if (button == 1) {
                        col.expandedModule = (col.expandedModule == mod) ? null : mod;
                    }
                    return true;
                }
                ry += ROW_H;

                // Settings drawer click
                if (col.expandedModule == mod) {
                    if (my >= ry && my < ry + SETTINGS_H) {
                        int halfW = (layout().columnWidth() - 6) / 2;
                        if (mx >= cx + 3 && mx <= cx + 3 + halfW) {
                            bindingModule = mod;
                        } else if (mx >= cx + 3 + halfW + 2 && mx <= cx + layout().columnWidth() - 3) {
                            openSettingsFor(mod);
                        }
                        return true;
                    }
                    ry += SETTINGS_H;
                }
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        // Find which column the mouse is over
        ClickGUILayout.Layout l = layout();
        for (Column col : columns) {
            int cx = col.x;
            int cy = col.y;
            List<Module> mods = col.filteredModules(searchQuery);
            int totalH = HEADER_H + Math.min(l.maxVisibleRows(), mods.size()) * ROW_H;

            if (mx >= cx && mx <= cx + l.columnWidth() && my >= cy && my <= cy + totalH) {
                if (vScroll > 0) {
                    col.scrollOffset = l.clampScrollOffset(col.scrollOffset - 1, mods.size());
                } else if (vScroll < 0) {
                    col.scrollOffset = l.clampScrollOffset(col.scrollOffset + 1, mods.size());
                }
                return true;
            }
        }
        return super.mouseScrolled(mx, my, hScroll, vScroll);
    }

    // ── keyboard handling ──────────────────────────────────────────────
    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        // Keybind recording
        if (bindingModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                bindingModule.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                bindingModule.setKeybind(key);
            }
            ProfileManager.getInstance().onModuleChanged();
            bindingModule = null;
            return true;
        }

        // Search typing
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                setSearchQuery(searchQuery.substring(0, searchQuery.length() - 1));
                return true;
            }
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                setSearchQuery("");
                return true;
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char ch, int modifiers) {
        if (searchFocused && ch >= 32) {
            setSearchQuery(searchQuery + ch);
            return true;
        }
        return super.charTyped(ch, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── helpers ────────────────────────────────────────────────────────
    private String settingsLabelFor(Module mod) {
        String n = mod.getName().toLowerCase();
        if (n.equals("xray")) return "§fOres ⚙";
        if (n.equals("seedcracker")) return "§fConfig ⚙";
        return "§7Info";
    }

    private void openSettingsFor(Module mod) {
        String n = mod.getName().toLowerCase();
        if (n.equals("xray")) {
            Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
        } else if (n.equals("seedcracker")) {
            Minecraft.getInstance().setScreen(new SeedCrackerConfigScreen(this));
        }
    }

    static String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return "None";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT   -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT    -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL  -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT     -> "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT      -> "LALT";
            case GLFW.GLFW_KEY_SPACE         -> "SPACE";
            case GLFW.GLFW_KEY_TAB           -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK     -> "CAPS";
            case GLFW.GLFW_KEY_F1  -> "F1";  case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";  case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";  case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";  case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";  case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_INSERT -> "INS"; case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_HOME -> "HOME"; case GLFW.GLFW_KEY_END -> "END";
            default -> "K" + key;
        };
    }

    // ── column model ───────────────────────────────────────────────────
    private static class Column {
        final Category category;
        Module expandedModule = null;
        int scrollOffset = 0;
        int x, y;                   // position (for future dragging support)

        Column(Category cat) {
            this.category = cat;
        }

        List<Module> filteredModules(String query) {
            List<Module> all = ModuleManager.getModules(category);
            if (query == null || query.isEmpty()) return all;
            String q = query.toLowerCase();
            List<Module> out = new ArrayList<>();
            for (Module m : all) {
                if (m.getName().toLowerCase().contains(q)) out.add(m);
            }
            return out;
        }

        int expandedExtra(List<Module> mods) {
            if (expandedModule != null && mods.contains(expandedModule)) return SETTINGS_H;
            return 0;
        }
    }
}
