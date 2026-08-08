package net.aetheris.client.gui;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

import net.aetheris.client.settings.AetherisLang;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.ModeSetting;
import net.aetheris.client.settings.Setting;
import net.aetheris.client.settings.SliderSetting;

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
    private static final int SEARCH_W    = 140;
    private static final int SEARCH_H    = 18;

    // ── state ──────────────────────────────────────────────────────────
    private static final java.util.Map<Category, int[]> SAVED_POSITIONS = new java.util.EnumMap<>(Category.class);

    private final List<Column> columns = new ArrayList<>();
    private Module bindingModule = null;
    private Module infoModule = null;
    private String searchQuery = "";
    private boolean searchFocused = false;
    private long openTime = 0;

    // Drag state
    private Column draggingColumn = null;
    private SliderSetting draggingSetting = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private boolean draggingModal = false;
    private int modalX = -1;
    private int modalY = -1;
    private int modalDragOffsetX = 0;
    private int modalDragOffsetY = 0;

    private static int settingsHeight(Module mod) {
        return 20;
    }

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
        draggingColumn = null;
        bindingModule = null;
        searchFocused = false;
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
        int maxColX = Math.max(0, width - layout.columnWidth());
        int maxColY = Math.max(0, height - HEADER_H);

        for (int index = 0; index < columns.size(); index++) {
            Column column = columns.get(index);
            if (!column.positioned) {
                int[] saved = SAVED_POSITIONS.get(column.category);
                if (saved != null) {
                    column.x = saved[0];
                    column.y = saved[1];
                } else {
                    column.x = layout.columnX(index);
                    column.y = layout.columnTopY();
                }
                column.positioned = true;
            }
            // Clamp column coordinates within screen boundaries
            column.x = Math.max(0, Math.min(column.x, maxColX));
            column.y = Math.max(0, Math.min(column.y, maxColY));
            SAVED_POSITIONS.put(column.category, new int[]{column.x, column.y});

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
        KEYBINDS("Keybinds"), XRAY("Xray"), ALTS("Alts"), SEED("Seed"), LANG("Lang");

        private final String label;
        UtilityAction(String label) { this.label = label; }
    }

    private void openUtility(UtilityAction action) {
        switch (action) {
            case KEYBINDS -> Minecraft.getInstance().setScreen(new KeybindManagerScreen(this));
            case XRAY -> Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
            case ALTS -> Minecraft.getInstance().setScreen(new AltManagerScreen(this));
            case SEED -> Minecraft.getInstance().setScreen(new SeedCrackerConfigScreen(this));
            case LANG -> {
                AetherisLang.toggle();
                ProfileManager.getInstance().save();
            }
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

        // ── Binding notice (positioned above dock to prevent search bar collision) ──
        if (bindingModule != null) {
            String notice = "§e⌨ Press key for §f" + bindingModule.getName() + " §7(ESC = clear)";
            g.drawCenteredString(font, notice, width / 2, height - SEARCH_H - 42, 0xFFFFFF00);
        }

        // ── Watermark ──
        g.drawString(font, "§7Aetheris §8v1.0", 4, height - 12, 0xFF505050);

        // ── Aetheris Header and Dock ──
        int activeCount = 0;
        for (Module m : ModuleManager.getModules()) if (m.isEnabled()) activeCount++;
        String headerText = "AETHERIS §8[" + activeCount + "]";
        g.drawCenteredString(font, headerText, width / 2, 10, 0xFFFFFFFF);

        int dockW = 250;
        int dockH = 16;
        int dockX = (width - dockW) / 2;
        int dockY = height - SEARCH_H - 10 - dockH - 4; // Above search bar
        
        int btnW = (dockW - 16) / 5;
        UtilityAction[] actions = UtilityAction.values();
        for (int i = 0; i < actions.length; i++) {
            UtilityAction action = actions[i];
            int bx = dockX + i * (btnW + 4);
            boolean hover = mouseX >= bx && mouseX < bx + btnW && mouseY >= dockY && mouseY < dockY + dockH;
            g.fill(bx, dockY, bx + btnW, dockY + dockH, hover ? 0xFF3A4050 : 0xFF2A3040);
            String label = action == UtilityAction.LANG ? AetherisLang.getLabel() : action.label;
            g.drawCenteredString(font, label, bx + btnW / 2, dockY + 4, 0xFFCCCCCC);
        }

        // ── Info Modal Overlay ──
        renderInfoModal(g, mouseX, mouseY);
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
            if (eIdx >= startIdx && eIdx < endIdx) expandedExtra = settingsHeight(col.expandedModule);
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

            // Calculate reserved right-hand space for Keybind and Arrow to prevent overflow/overlap
            int rightReserved = 10; // Margin + expansion arrow
            if (mod.getKeybind() != GLFW.GLFW_KEY_UNKNOWN) {
                String kn = keyName(mod.getKeybind());
                int tw = font.width(kn);
                rightReserved += tw + 4;
                // Keybind label (right-aligned before arrow)
                g.drawString(font, kn, cx + layout().columnWidth() - 10 - tw, ry + 4, 0xFF555555);
            }

            // Expand arrow indicator
            String arrow = (col.expandedModule == mod) ? "▾" : "▸";
            g.drawString(font, arrow, cx + layout().columnWidth() - 8, ry + 4, 0xFF666666);

            // Module name truncated strictly within available width
            int textCol = enabled ? 0xFFFFFFFF : (hover ? 0xFFDDDDDD : 0xFFAAAAAA);
            String displayName = mod.getName();
            int maxNameWidth = layout().columnWidth() - 6 - rightReserved;
            if (font.width(displayName) > maxNameWidth) {
                int ellipsisW = font.width("…");
                int availableForText = Math.max(1, maxNameWidth - ellipsisW);
                displayName = font.plainSubstrByWidth(displayName, availableForText) + "…";
            }
            g.drawString(font, displayName, cx + 6, ry + 4, textCol);

            ry += ROW_H;

            // ── Expanded settings drawer (inline, Meteor-inspired) ──
            if (col.expandedModule == mod) {
                int sh = settingsHeight(mod);
                renderSettingsDrawer(g, cx, ry, mod, mouseX, mouseY, sh);
                ry += sh;
            }
        }

        // Scroll down indicator
        if (endIdx < mods.size()) {
            g.drawCenteredString(font, "§7▼", cx + layout().columnWidth() / 2, ry + 1, 0xFF808080);
        }

        // Bottom border accent line
        g.fill(cx, cy + totalH - 1, cx + layout().columnWidth(), cy + totalH, (accent & 0x00FFFFFF) | 0x30000000);
    }

    private void renderSettingsDrawer(GuiGraphics g, int cx, int ry, Module mod, int mouseX, int mouseY, int totalHeight) {
        // Drawer background — slightly indented, darker
        g.fill(cx + 3, ry, cx + layout().columnWidth() - 3, ry + totalHeight, 0xE8101420);
        g.fill(cx + 3, ry, cx + layout().columnWidth() - 3, ry + 1, 0x40FFFFFF);   // top separator line

        int halfW = (layout().columnWidth() - 6) / 2;
        int btnY = ry + 2;
        int btnH = 20 - 4; // top bar is always 20px

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
        if (infoModule != null && button == 0) {
            ModalLayoutInfo layout = new ModalLayoutInfo(font, infoModule);
            int mW = layout.width;
            int mH = layout.height;
            if (modalX == -1 || modalY == -1) {
                modalX = (width - mW) / 2;
                modalY = (height - mH) / 2;
            }
            int mX = modalX;
            int mY = modalY;

            // X close button check
            if (mx >= mX + mW - 22 && mx <= mX + mW - 6 && my >= mY + 4 && my <= mY + 20) {
                infoModule = null;
                modalX = -1; modalY = -1; draggingModal = false;
                return true;
            }

            // Header drag start
            if (mx >= mX && mx <= mX + mW - 24 && my >= mY && my <= mY + 24) {
                draggingModal = true;
                modalDragOffsetX = (int) mx - mX;
                modalDragOffsetY = (int) my - mY;
                return true;
            }

            // Status toggle button check
            int btnX = mX + 10;
            int btnY = mY + 30;
            int btnW = 130;
            int btnH = 18;
            if (mx >= btnX && mx <= btnX + btnW && my >= btnY && my <= btnY + btnH) {
                infoModule.toggle();
                return true;
            }

            // Modal settings interaction
            if (infoModule.hasSettings()) {
                int sy = mY + layout.settingsStartY + 16;
                for (Setting<?> setting : infoModule.getSettings()) {
                    if (my >= sy && my < sy + 22) {
                        if (setting instanceof BooleanSetting bs) {
                            bs.toggle();
                            ProfileManager.getInstance().save();
                        } else if (setting instanceof ModeSetting<?> ms) {
                            ms.cycle();
                            ProfileManager.getInstance().save();
                        } else if (setting instanceof SliderSetting ss) {
                            draggingSetting = ss;
                            String valText = ss.getValueDisplay();
                            int valW = font.width(valText);
                            int trackW = 90;
                            int trackX = mX + mW - 20 - valW - trackW;
                            double newRatio = (double) (mx - trackX) / trackW;
                            newRatio = Math.max(0.0, Math.min(1.0, newRatio));
                            ss.setFromRatio(newRatio);
                        }
                        return true;
                    }
                    sy += 22;
                }
            }

            // Click outside modal
            if (mx < mX || mx > mX + mW || my < mY || my > mY + mH) {
                infoModule = null;
                modalX = -1; modalY = -1; draggingModal = false;
                return true;
            }
            return true;
        }

        // Utility dock (check BEFORE binding dismiss so dock always works)
        int dockW = 250;
        int dockH = 16;
        int dockX = (width - dockW) / 2;
        int dockY = height - SEARCH_H - 10 - dockH - 4;
        if (my >= dockY && my < dockY + dockH && button == 0) {
            int btnW = (dockW - 16) / 5;
            for (int i = 0; i < 5; i++) {
                int bx = dockX + i * (btnW + 4);
                if (mx >= bx && mx < bx + btnW) {
                    openUtility(UtilityAction.values()[i]);
                    return true;
                }
            }
        }

        // Dismiss binding on stray click
        if (bindingModule != null && button == 0) {
            bindingModule = null;
            return true;
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

        // Column header drag start (move column to end of list so it renders in foreground)
        if (button == 0) {
            for (int i = columns.size() - 1; i >= 0; i--) {
                Column col = columns.get(i);
                if (mx >= col.x && mx <= col.x + layout().columnWidth()
                    && my >= col.y && my <= col.y + HEADER_H) {
                    draggingColumn = col;
                    dragOffsetX = (int) mx - col.x;
                    dragOffsetY = (int) my - col.y;
                    columns.remove(i);
                    columns.add(col);
                    return true;
                }
            }
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
                    if (my >= ry && my < ry + 20) {
                        int halfW = (layout().columnWidth() - 6) / 2;
                        if (mx >= cx + 3 && mx <= cx + 3 + halfW) {
                            bindingModule = mod;
                        } else if (mx >= cx + 3 + halfW + 2 && mx <= cx + layout().columnWidth() - 3) {
                            openSettingsFor(mod);
                        }
                        return true;
                    }
                    ry += 20;
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

    @Override
    public boolean mouseDragged(double mx, double my, int button, double deltaX, double deltaY) {
        if (draggingColumn != null && button == 0) {
            int maxColX = Math.max(0, width - layout().columnWidth());
            int maxColY = Math.max(0, height - HEADER_H);
            int newX = (int) mx - dragOffsetX;
            int newY = (int) my - dragOffsetY;
            draggingColumn.x = Math.max(0, Math.min(newX, maxColX));
            draggingColumn.y = Math.max(0, Math.min(newY, maxColY));
            draggingColumn.positioned = true;
            SAVED_POSITIONS.put(draggingColumn.category, new int[]{draggingColumn.x, draggingColumn.y});
            return true;
        }
        return super.mouseDragged(mx, my, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (draggingModal && button == 0) {
            draggingModal = false;
            return true;
        }
        if (draggingSetting != null && button == 0) {
            draggingSetting = null;
            ProfileManager.getInstance().save();
            return true;
        }
        if (draggingColumn != null && button == 0) {
            draggingColumn = null;
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    // ── keyboard handling ──────────────────────────────────────────────
    @Override
    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (infoModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                infoModule = null;
                return true;
            }
        }

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
    public void onClose() {
        draggingColumn = null;
        bindingModule = null;
        infoModule = null;
        searchFocused = false;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── helpers ────────────────────────────────────────────────────────
    private String settingsLabelFor(Module mod) {
        if (mod.hasSettings()) return "§fSettings ⚙";
        String n = mod.getName().toLowerCase();
        if (n.equals("xray")) return "§fSettings ⚙";
        if (n.equals("seedcracker")) return "§fConfig ⚙";
        return "§7Info";
    }

    private void openSettingsFor(Module mod) {
        String n = mod.getName().toLowerCase();
        if (n.equals("xray")) {
            Minecraft.getInstance().setScreen(new XraySettingsScreen(this));
        } else if (n.equals("seedcracker")) {
            Minecraft.getInstance().setScreen(new SeedCrackerConfigScreen(this));
        } else {
            this.infoModule = mod;
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

    // ── Info & Settings Modal Overlay ──────────────────────────────────
    private static class ModalLayoutInfo {
        final int width = 340;
        final int height;
        final int descHeight;
        final int settingsStartY;
        final String activeDesc;

        ModalLayoutInfo(Font font, Module mod) {
            this.activeDesc = AetherisLang.isIT() ? getModuleDescIT(mod) : getModuleDescEN(mod);
            List<FormattedCharSequence> descLines = font.split(Component.literal(activeDesc), width - 20);
            this.descHeight = Math.max(18, descLines.size() * 10);
            int settingsCount = mod.hasSettings() ? mod.getSettings().size() : 0;
            int settingsExtra = settingsCount > 0 ? (24 + settingsCount * 22) : 0;
            this.height = 100 + descHeight + settingsExtra;
            this.settingsStartY = 88 + descHeight;
        }
    }

    private void renderInfoModal(GuiGraphics g, int mouseX, int mouseY) {
        if (infoModule == null) return;

        ModalLayoutInfo layout = new ModalLayoutInfo(font, infoModule);
        int mW = layout.width;
        int mH = layout.height;

        // Dark dim backdrop for modal
        g.fill(0, 0, width, height, 0xE0000000);

        if (modalX == -1 || modalY == -1) {
            modalX = (width - mW) / 2;
            modalY = (height - mH) / 2;
        }

        if (draggingModal) {
            modalX = mouseX - modalDragOffsetX;
            modalY = mouseY - modalDragOffsetY;
        }

        int mX = modalX;
        int mY = modalY;
        int accent = accentOf(infoModule.getCategory());

        // Card shadow & 100% FULLY OPAQUE solid dark background (no text bleed-through)
        g.fill(mX + 4, mY + 4, mX + mW + 4, mY + mH + 4, 0xCC000000);
        g.fill(mX, mY, mX + mW, mY + mH, 0xFF0F131D);

        // Header bar with accent line
        g.fill(mX, mY, mX + mW, mY + 24, 0xFF181E2A);
        g.fill(mX, mY, mX + mW, mY + 2, accent);

        // Title text
        String titleText = "§l" + infoModule.getName() + " §8[" + infoModule.getCategory().getName() + "]";
        g.drawString(font, titleText, mX + 10, mY + 7, 0xFFFFFFFF);

        // Close 'X' button on header
        boolean hoverX = mouseX >= mX + mW - 22 && mouseX <= mX + mW - 6 && mouseY >= mY + 4 && mouseY <= mY + 20;
        g.drawString(font, "§c✖", mX + mW - 16, mY + 7, hoverX ? 0xFFFF5555 : 0xFFAAAA);

        // Status & Toggle Button
        boolean enabled = infoModule.isEnabled();
        int btnX = mX + 10;
        int btnY = mY + 30;
        int btnW = 130;
        int btnH = 18;
        boolean hoverToggle = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        int btnBg = enabled ? (hoverToggle ? 0xFF358035 : 0xFF256025) : (hoverToggle ? 0xFF803535 : 0xFF602525);
        g.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        String toggleLabel = enabled ? (AetherisLang.isIT() ? "✔ ABILITATO (ON)" : "✔ ENABLED (ON)") : (AetherisLang.isIT() ? "✖ DISABILITATO (OFF)" : "✖ DISABLED (OFF)");
        g.drawCenteredString(font, toggleLabel, btnX + btnW / 2, btnY + 5, 0xFFFFFFFF);

        // Keybind info badge
        String keyText = (AetherisLang.isIT() ? "Tasto: §e" : "Key: §e") + keyName(infoModule.getKeybind());
        g.drawString(font, keyText, mX + 150, mY + 35, 0xFFCCCCCC);

        // Separator line
        g.fill(mX + 10, mY + 54, mX + mW - 10, mY + 55, 0x40FFFFFF);

        // Active Language Description (Single Language display based on active toggle)
        int curY = mY + 60;
        if (AetherisLang.isIT()) {
            g.drawString(font, "§6🇮🇹 Descrizione:", mX + 10, curY, 0xFFFFAA00);
            curY += 12;
            drawWrappedText(g, layout.activeDesc, mX + 10, curY, mW - 20, 0xFFDDDDDD);
        } else {
            g.drawString(font, "§b🇬🇧 Description:", mX + 10, curY, 0xFF55FFFF);
            curY += 12;
            drawWrappedText(g, layout.activeDesc, mX + 10, curY, mW - 20, 0xFFBBBBBB);
        }

        // ── Render Full Settings inside Modal Card ──
        if (infoModule.hasSettings()) {
            curY = mY + layout.settingsStartY;
            g.fill(mX + 10, curY - 4, mX + mW - 10, curY - 3, 0x40FFFFFF);
            String settingsHeader = AetherisLang.isIT() ? "§e⚙ Impostazioni:" : "§e⚙ Settings:";
            g.drawString(font, settingsHeader, mX + 10, curY, 0xFFFFAA00);
            curY += 16;

            for (Setting<?> setting : infoModule.getSettings()) {
                boolean hover = mouseX >= mX + 10 && mouseX <= mX + mW - 10
                             && mouseY >= curY && mouseY < curY + 20;
                if (hover) {
                    g.fill(mX + 10, curY, mX + mW - 10, curY + 20, 0x1AFFFFFF);
                }

                // Full, un-truncated label in active language
                g.drawString(font, setting.getDisplayName(), mX + 14, curY + 5, 0xFFFFFFFF);

                if (setting instanceof BooleanSetting bs) {
                    String toggle = bs.isOn() ? "§a[✔ ON]" : "§c[✖ OFF]";
                    int tw = font.width(toggle);
                    g.drawString(font, toggle, mX + mW - 14 - tw, curY + 5, 0xFFFFFFFF);
                } else if (setting instanceof ModeSetting<?> ms) {
                    String modeText = "§7< §f" + ms.getValueDisplay() + " §7>";
                    int tw = font.width(modeText);
                    g.drawString(font, modeText, mX + mW - 14 - tw, curY + 5, 0xFFFFFFFF);
                } else if (setting instanceof SliderSetting ss) {
                    String valText = ss.getValueDisplay();
                    int valW = font.width(valText);
                    g.drawString(font, valText, mX + mW - 14 - valW, curY + 5, 0xFF55FFFF);

                    int trackW = 90;
                    int trackX = mX + mW - 20 - valW - trackW;
                    int trackY = curY + 7;
                    g.fill(trackX, trackY, trackX + trackW, trackY + 5, 0xFF111111);
                    int fillW = (int) (ss.getRatio() * trackW);
                    g.fill(trackX, trackY, trackX + fillW, trackY + 5, accent);

                    if (draggingSetting == ss) {
                        double newRatio = (double) (mouseX - trackX) / trackW;
                        newRatio = Math.max(0.0, Math.min(1.0, newRatio));
                        ss.setFromRatio(newRatio);
                    }
                }
                curY += 22;
            }
        }

        // Footer note
        String footerMsg = AetherisLang.isIT() ? "§7(ESC o clicca fuori per chiudere)" : "§7(ESC or click outside to close)";
        g.drawString(font, footerMsg, mX + 10, mY + mH - 14, 0xFF666666);
    }

    private void drawWrappedText(GuiGraphics g, String text, int x, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return;
        List<FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth);
        int curY = y;
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, x, curY, color);
            curY += 9;
        }
    }

    private static String getModuleDescIT(Module mod) {
        String n = mod.getName().toLowerCase();
        return switch (n) {
            case "killaura" -> "Attacca automaticamente tutte le entità ostili nel tuo raggio d'azione.";
            case "velocity" -> "Annulla il rinculo (knockback) subito quando vieni colpito.";
            case "criticals" -> "Forza colpi critici ad ogni attacco senza bisogno di saltare.";
            case "reach" -> "Aumenta la portata. • Portata Combattimento: distanza max per colpire mob (Max 6.0m limite server). • Portata Blocchi: distanza max per piazzare/rompere blocchi.";
            case "autoarmor" -> "Equipaggia automaticamente la migliore armatura nell'inventario.";
            case "autototem" -> "Mette automaticamente un Totem della non-morte nella mano secondaria.";
            case "triggerbot" -> "Attacca automaticamente l'entità quando la miri col mirino.";
            case "surround" -> "Piazza rapidamente blocchi di ossidiana attorno ai tuoi piedi.";
            case "autosprint" -> "Mantiene la corsa (sprint) sempre attiva mentre ti muovi.";
            case "speed" -> "Aumenta la velocità di movimento a terra del personaggio.";
            case "fly" -> "Ti permette di volare liberamente anche in sopravvivenza.";
            case "nofall" -> "Elimina completamente tutti i danni subiti da caduta dall'alto.";
            case "step" -> "Permette di salire i blocchi d'altezza senza dover saltare.";
            case "noslowdown" -> "Impedisce i rallentamenti quando mangi o usi l'arco.";
            case "noclip" -> "Ti permette di camminare ed attraversare i blocchi solidi.";
            case "fullbright" -> "Porta la luminosità al massimo rendendo visibili le caverne buie.";
            case "esp" -> "Mostra il contorno di giocatori ed entità anche dietro i muri.";
            case "nohurtcam" -> "Rimuove l'effetto di scuotimento della telecamera quando subisci danno.";
            case "xray" -> "Rende trasparenti i blocchi comuni per evidenziare i minerali.";
            case "nametags" -> "Mostra nomi e vita dei giocatori ingranditi attraverso i muri.";
            case "tracers" -> "Disegna linee dal mirino verso le posizioni degli altri giocatori.";
            case "freecam" -> "Separa la telecamera dal corpo per esplorare in modalità spettatore.";
            case "fastbreak" -> "Velocizza lo scavo dei blocchi accelerandone la rottura.";
            case "scaffold" -> "Piazza automaticamente blocchi sotto i piedi mentre cammini nell'aria.";
            case "timer" -> "Modifica la velocità generale del ciclo di gioco (TPS locali).";
            case "autotool" -> "Seleziona l'attrezzo migliore nella barra rapida per il blocco che scavi.";
            case "installedplugins" -> "Analizza i comandi inviati dal server per rilevare i plugin installati.";
            case "autorespawn" -> "Rinasce automaticamente all'istante senza dover cliccare alla morte.";
            case "fastplace" -> "Rimuove il ritardo nel piazzamento continuo tenendo premuto il mouse.";
            case "nohunger" -> "Previene o riduce il consumo della barra della fame durante le azioni.";
            case "cheststealer" -> "Svuota e sposta tutti gli oggetti dalle casse aperte nel tuo inventario.";
            case "autofish" -> "Pesca e rilancia la canna da pesca in modo automatico.";
            case "inventorycleaner" -> "Scarta automaticamente gli oggetti inutili o doppioni dall'inventario.";
            case "seedcracker" -> "Ricostruisce il seed del mondo analizzando le strutture del server.";
            default -> mod.getDescription().isEmpty() ? "Nessuna descrizione disponibile." : mod.getDescription();
        };
    }

    private static String getModuleDescEN(Module mod) {
        String n = mod.getName().toLowerCase();
        return switch (n) {
            case "killaura" -> "Automatically attacks all hostile entities around you.";
            case "velocity" -> "Cancels knockback received from attacks or explosions.";
            case "criticals" -> "Forces critical hits on every attack without needing to jump.";
            case "reach" -> "Increases reach distance. • Combat Reach: max distance to attack mobs (Max 6.0m server limit). • Block Reach: max distance to place/break blocks.";
            case "autoarmor" -> "Automatically equips the best armor pieces in your inventory.";
            case "autototem" -> "Automatically places a Totem of Undying in your offhand.";
            case "triggerbot" -> "Automatically attacks entities whenever your crosshair hits them.";
            case "surround" -> "Quickly places obsidian blocks around your feet for protection.";
            case "autosprint" -> "Keeps sprinting enabled continuously while moving.";
            case "speed" -> "Increases your ground movement speed.";
            case "fly" -> "Allows you to fly freely even in survival mode.";
            case "nofall" -> "Prevents all fall damage from high drops.";
            case "step" -> "Allows walking up block ledges without jumping.";
            case "noslowdown" -> "Prevents movement slowdown while eating or using bows.";
            case "noclip" -> "Allows walking through solid walls and blocks.";
            case "fullbright" -> "Sets maximum brightness, illuminating dark areas and caves.";
            case "esp" -> "Highlights players and entities through solid walls.";
            case "nohurtcam" -> "Removes the camera shake wobble when taking damage.";
            case "xray" -> "Makes common blocks transparent to reveal hidden ores.";
            case "nametags" -> "Renders enlarged, readable player tags and health through walls.";
            case "tracers" -> "Draws lines from your crosshair to all surrounding players.";
            case "freecam" -> "Detaches your camera from your body for free spectator viewing.";
            case "fastbreak" -> "Increases block mining speed.";
            case "scaffold" -> "Automatically builds blocks beneath your feet as you walk in mid-air.";
            case "timer" -> "Changes the overall game tick speed.";
            case "autotool" -> "Automatically switches to the best tool for the targeted block.";
            case "installedplugins" -> "Intercepts server packets to discover and list installed plugins.";
            case "autorespawn" -> "Instantly respawns upon death without pressing any button.";
            case "fastplace" -> "Removes placement delay when holding right-click.";
            case "nohunger" -> "Reduces food bar exhaustion during actions and sprinting.";
            case "cheststealer" -> "Automatically loots all items from opened chests into your inventory.";
            case "autofish" -> "Automatically catches fish and recasts your fishing rod.";
            case "inventorycleaner" -> "Drops trash items and low-quality duplicates from inventory.";
            case "seedcracker" -> "Reconstructs the world seed by inspecting server structures.";
            default -> mod.getDescription().isEmpty() ? "No description available." : mod.getDescription();
        };
    }

    // ── column model ───────────────────────────────────────────────────
    private static class Column {
        final Category category;
        Module expandedModule = null;
        int scrollOffset = 0;
        int x, y;
        boolean positioned = false;

        Column(Category cat) {
            this.category = cat;
        }

        List<Module> filteredModules(String query) {
            List<Module> all = ModuleManager.getModules(category);
            if (all == null) return List.of();
            if (query == null || query.isEmpty()) return all;
            String q = query.toLowerCase();
            List<Module> out = new ArrayList<>();
            for (Module m : all) {
                if (m != null && m.getName() != null && m.getName().toLowerCase().contains(q)) {
                    out.add(m);
                }
            }
            return out;
        }

        int expandedExtra(List<Module> mods) {
            if (expandedModule != null && mods != null && mods.contains(expandedModule)) {
                return settingsHeight(expandedModule);
            }
            return 0;
        }
    }
}
