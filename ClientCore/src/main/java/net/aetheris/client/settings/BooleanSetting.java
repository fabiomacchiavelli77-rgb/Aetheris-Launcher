package net.aetheris.client.settings;

/**
 * Boolean toggle setting (on/off).
 * Used for settings like "Target Players", "Night Vision", "Auto Jump", etc.
 */
public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String id, String nameEN, String nameIT, boolean defaultValue) {
        super(id, nameEN, nameIT, defaultValue);
    }

    public boolean isOn() { return getValue(); }

    public void toggle() { setValue(!getValue()); }

    @Override
    public String getType() { return "boolean"; }

    @Override
    public String getValueDisplay() {
        if (AetherisLang.isIT()) {
            return getValue() ? "§aON" : "§cOFF";
        }
        return getValue() ? "§aON" : "§cOFF";
    }
}
