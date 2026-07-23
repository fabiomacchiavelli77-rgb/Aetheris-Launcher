package net.aetheris.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ProfileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Minecraft.getInstance().gameDirectory.toPath().resolve("aetheris");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("profile.json");
    private static ProfileManager instance;
    private boolean autoSaveEnabled = true;

    public static ProfileManager getInstance() {
        if (instance == null) instance = new ProfileManager();
        return instance;
    }

    public void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) return;

            String json = Files.readString(CONFIG_FILE);
            Map<String, ModuleState> states = GSON.fromJson(json,
                new TypeToken<Map<String, ModuleState>>(){}.getType());

            if (states == null) return;

            for (Module module : ModuleManager.getModules()) {
                ModuleState state = states.get(module.getName());
                if (state != null) {
                    if (state.enabled) module.setEnabled(true);
                    module.setKeybind(state.keybind);
                }
            }
        } catch (Exception e) {
            System.err.println("[Aetheris] Failed to load profile: " + e.getMessage());
        }
    }

    public void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);

            Map<String, ModuleState> states = new LinkedHashMap<>();
            for (Module module : ModuleManager.getModules()) {
                states.put(module.getName(), new ModuleState(module.isEnabled(), module.getKeybind()));
            }

            String json = GSON.toJson(states);
            Files.writeString(CONFIG_FILE, json);
        } catch (Exception e) {
            System.err.println("[Aetheris] Failed to save profile: " + e.getMessage());
        }
    }

    public void onModuleChanged() {
        if (autoSaveEnabled) save();
    }

    public static class ModuleState {
        public boolean enabled;
        public int keybind;

        public ModuleState(boolean enabled, int keybind) {
            this.enabled = enabled;
            this.keybind = keybind;
        }
    }
}
