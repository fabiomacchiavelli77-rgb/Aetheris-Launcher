package net.aetheris.client.settings;

/**
 * Abstract base class for all module settings.
 * Supports bilingual labels (IT/EN) for the Aetheris GUI.
 *
 * @param <T> the value type of this setting
 */
public abstract class Setting<T> {
    private final String id;
    private final String nameEN;
    private final String nameIT;
    private T value;
    private final T defaultValue;

    public Setting(String id, String nameEN, String nameIT, T defaultValue) {
        this.id = id;
        this.nameEN = nameEN;
        this.nameIT = nameIT;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
    }

    public String getId() { return id; }
    public String getNameEN() { return nameEN; }
    public String getNameIT() { return nameIT; }

    /** Returns the display name based on the current language */
    public String getDisplayName() {
        return AetherisLang.isIT() ? nameIT : nameEN;
    }

    public T getValue() { return value; }
    public T getDefaultValue() { return defaultValue; }

    public void setValue(T value) { this.value = value; }
    public void reset() { this.value = defaultValue; }

    /** Returns the setting type identifier for GUI rendering */
    public abstract String getType(); // "slider", "boolean", "mode"

    /** Returns a display string for the current value */
    public abstract String getValueDisplay();
}
