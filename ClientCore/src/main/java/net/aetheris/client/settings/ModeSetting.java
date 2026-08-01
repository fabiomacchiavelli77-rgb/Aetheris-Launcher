package net.aetheris.client.settings;

/**
 * Enum-based mode/dropdown setting.
 * Used for settings like "Mode: Packet/Jump/MiniJump", "Priority: Distance/Health", etc.
 *
 * @param <E> the Enum type containing the available modes
 */
public class ModeSetting<E extends Enum<E>> extends Setting<E> {
    private final E[] values;

    public ModeSetting(String id, String nameEN, String nameIT, E defaultValue) {
        super(id, nameEN, nameIT, defaultValue);
        @SuppressWarnings("unchecked")
        E[] vals = (E[]) defaultValue.getClass().getEnumConstants();
        this.values = vals;
    }

    public E[] getValues() { return values; }

    /** Cycle to next mode value */
    public void cycle() {
        E current = getValue();
        int idx = current.ordinal();
        setValue(values[(idx + 1) % values.length]);
    }

    /** Cycle to previous mode value */
    public void cycleBack() {
        E current = getValue();
        int idx = current.ordinal();
        setValue(values[(idx - 1 + values.length) % values.length]);
    }

    @Override
    public String getType() { return "mode"; }

    @Override
    public String getValueDisplay() {
        return "§e" + getValue().name();
    }
}
