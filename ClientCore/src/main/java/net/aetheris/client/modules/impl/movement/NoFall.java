package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class NoFall extends Module {
    private final SliderSetting fallThreshold = new SliderSetting("fallThreshold", "Fall Distance", "Distanza Caduta", 2.5, 1.5, 5.0, 0.5, "blocks");

    public NoFall() {
        super("NoFall", "Previene il danno da caduta modificando i pacchetti di movimento in uscita.", Category.MOVEMENT);
        addSetting(fallThreshold);
    }

    public float getFallThreshold() {
        return fallThreshold.getValue().floatValue();
    }
}
