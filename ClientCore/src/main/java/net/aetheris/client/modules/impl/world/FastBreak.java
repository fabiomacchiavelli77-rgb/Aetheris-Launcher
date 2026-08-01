package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class FastBreak extends Module {
    private final SliderSetting multiplier = new SliderSetting("multiplier", "Speed Multiplier", "Moltiplicatore Velocità", 1.5, 1.0, 5.0, 0.1, "x");

    public FastBreak() {
        super("FastBreak", "Rompe i blocchi più velocemente.", Category.WORLD);
        addSetting(multiplier);
    }

    public float getSpeedMultiplier() { return multiplier.getValue().floatValue(); }
}
