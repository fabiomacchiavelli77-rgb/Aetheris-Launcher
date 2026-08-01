package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class Velocity extends Module {

    private final SliderSetting horizontal = new SliderSetting("horizontal", "Horizontal %", "Orizzontale %", 0, 0, 100, 1, "%");
    private final SliderSetting vertical = new SliderSetting("vertical", "Vertical %", "Verticale %", 0, 0, 100, 1, "%");

    public Velocity() {
        super("Velocity", "Annulla il knockback subito.", Category.COMBAT);
        addSetting(horizontal);
        addSetting(vertical);
    }
    
    public float getHorizontal() {
        return horizontal.getValue().floatValue();
    }
    
    public float getVertical() {
        return vertical.getValue().floatValue();
    }
}
