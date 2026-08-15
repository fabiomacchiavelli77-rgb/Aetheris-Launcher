package net.aetheris.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.settings.AetherisLang;
import net.aetheris.client.settings.Setting;
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

    @SuppressWarnings("unchecked")
    public void load() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) return;

            String json = Files.readString(CONFIG_FILE);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root == null) return;
            
            ProfileData data = new ProfileData();
            if (root.has("modules")) {
                data = GSON.fromJson(root, ProfileData.class);
            } else {
                // Fallback for old format
                Map<String, ModuleState> states = GSON.fromJson(root, new TypeToken<Map<String, ModuleState>>(){}.getType());
                if (states != null) data.modules = states;
            }

            if (data.language != null) {
                try {
                    AetherisLang.set(AetherisLang.Language.valueOf(data.language));
                } catch (IllegalArgumentException ignored) {}
            }

            if (data.modules != null) {
                for (Module module : ModuleManager.getModules()) {
                    ModuleState state = data.modules.get(module.getName());
                    if (state != null) {
                        if (state.enabled) module.setEnabled(true);
                        module.setKeybind(state.keybind);
                        
                        if (state.settings != null && module.hasSettings()) {
                            for (Setting<?> setting : module.getSettings()) {
                                Object savedVal = state.settings.get(setting.getId());
                                if (savedVal != null) {
                                    try {
                                        if (setting.getType().equals("slider") && savedVal instanceof Number) {
                                            ((Setting<Double>) setting).setValue(((Number) savedVal).doubleValue());
                                        } else if (setting.getType().equals("boolean") && savedVal instanceof Boolean) {
                                            ((Setting<Boolean>) setting).setValue((Boolean) savedVal);
                                        } else if (setting.getType().equals("mode") && savedVal instanceof String) {
                                            restoreEnumSetting(setting, (String) savedVal);
                                        }
                                    } catch (Exception e) {
                                        System.err.println("[Aetheris] Failed to restore setting " + setting.getId() + " for " + module.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Aetheris] Failed to load profile: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void restoreEnumSetting(Setting<?> setting, String savedVal) {
        E[] constants = (E[]) setting.getDefaultValue().getClass().getEnumConstants();
        if (constants != null) {
            for (E constant : constants) {
                if (constant.name().equals(savedVal)) {
                    ((Setting<E>) setting).setValue(constant);
                    break;
                }
            }
        }
    }

    public void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);

            ProfileData data = new ProfileData();
            data.language = AetherisLang.get().name();

            for (Module module : ModuleManager.getModules()) {
                ModuleState state = new ModuleState(module.isEnabled(), module.getKeybind());
                if (module.hasSettings()) {
                    for (Setting<?> setting : module.getSettings()) {
                        Object val = setting.getValue();
                        if (val instanceof Enum<?>) {
                            state.settings.put(setting.getId(), ((Enum<?>) val).name());
                        } else {
                            state.settings.put(setting.getId(), val);
                        }
                    }
                }
                data.modules.put(module.getName(), state);
            }

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_FILE, json);
        } catch (Exception e) {
            System.err.println("[Aetheris] Failed to save profile: " + e.getMessage());
        }
    }

    public void onModuleChanged() {
        if (autoSaveEnabled) save();
    }

    public static class ProfileData {
        public String language = "IT";
        public Map<String, ModuleState> modules = new LinkedHashMap<>();
    }

    public static class ModuleState {
        public boolean enabled;
        public int keybind;
        public Map<String, Object> settings = new LinkedHashMap<>();

        public ModuleState(boolean enabled, int keybind) {
            this.enabled = enabled;
            this.keybind = keybind;
        }
    }
}
