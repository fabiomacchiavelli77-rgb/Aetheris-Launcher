package net.aetheris.client.settings;

/**
 * Numeric slider setting with min/max/step bounds and optional unit display.
 * Used for settings like Range (blocks), Speed (multiplier), Delay (ticks), etc.
 */
public class SliderSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String unit; // "", "%", "x", "blocks", "ticks", "CPS", "HP", "°"

    public SliderSetting(String id, String nameEN, String nameIT, double defaultValue,
                         double min, double max, double step, String unit) {
        super(id, nameEN, nameIT, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.unit = unit;
    }

    public SliderSetting(String id, String nameEN, String nameIT, double defaultValue,
                         double min, double max, double step) {
        this(id, nameEN, nameIT, defaultValue, min, max, step, "");
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }
    public String getUnit() { return unit; }

    /** Returns value snapped to step grid */
    public double getSnappedValue() {
        double v = getValue();
        return Math.round(v / step) * step;
    }

    /** Returns value as int (for settings like delay in ticks) */
    public int getIntValue() { return (int) Math.round(getValue()); }

    /** Returns normalized 0.0–1.0 ratio for slider rendering */
    public double getRatio() {
        return (getValue() - min) / (max - min);
    }

    /** Sets value from a 0.0–1.0 ratio (used by slider drag) */
    public void setFromRatio(double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        double raw = min + ratio * (max - min);
        double snapped = Math.round(raw / step) * step;
        setValue(Math.max(min, Math.min(max, snapped)));
    }

    @Override
    public void setValue(Double value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    @Override
    public String getType() { return "slider"; }

    @Override
    public String getValueDisplay() {
        double v = getSnappedValue();
        if (step >= 1.0) {
            return String.format("%d%s", (int) v, unit);
        }
        return String.format("%.1f%s", v, unit);
    }
}
