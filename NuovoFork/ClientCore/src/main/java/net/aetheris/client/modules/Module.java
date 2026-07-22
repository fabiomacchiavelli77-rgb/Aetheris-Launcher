package net.aetheris.client.modules;

import net.minecraft.client.Minecraft;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    protected final Minecraft mc = Minecraft.getInstance();

    public Module(String name, Category category) {
        this(name, null, category);
    }

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description != null ? description : "";
        this.category = category;
        this.enabled = false;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) onEnable();
            else onDisable();
        }
    }

    public void toggle() { setEnabled(!enabled); }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
