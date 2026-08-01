package net.aetheris.client.modules;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.settings.Setting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keybind;
    protected final Minecraft mc = Minecraft.getInstance();

    // ── Settings system ──────────────────────────────────────────────
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, Category category) {
        this(name, null, category);
    }

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description != null ? description : "";
        this.category = category;
        this.enabled = false;
        this.keybind = GLFW.GLFW_KEY_UNKNOWN; // -1 = nessun tasto
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeybind() { return keybind; }

    public void setKeybind(int key) { this.keybind = key; }

    public String getKeybindName() {
        if (keybind == GLFW.GLFW_KEY_UNKNOWN) return "None";
        return GLFW.glfwGetKeyName(keybind, 0);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) onEnable();
            else onDisable();
            ProfileManager.getInstance().onModuleChanged();
        }
    }

    public void toggle() { setEnabled(!enabled); }

    // ── Settings API ─────────────────────────────────────────────────
    /** Register a setting for this module. Call in constructor. */
    protected void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    /** Returns an unmodifiable view of this module's settings */
    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    /** Returns true if this module has configurable settings */
    public boolean hasSettings() {
        return !settings.isEmpty();
    }

    /** Find a setting by its ID */
    public Setting<?> getSettingById(String id) {
        for (Setting<?> s : settings) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    // ── Lifecycle hooks ──────────────────────────────────────────────
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
