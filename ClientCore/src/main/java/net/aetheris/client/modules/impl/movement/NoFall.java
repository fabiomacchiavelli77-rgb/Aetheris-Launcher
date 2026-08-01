package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class NoFall extends Module {
    private final SliderSetting fallThreshold = new SliderSetting("fallThreshold", "Fall Threshold", "Soglia Caduta", 3.0, 2.0, 10.0, 0.5, "blocks");

    public NoFall() {
        super("NoFall", "Previene il danno da caduta.", Category.MOVEMENT);
        addSetting(fallThreshold);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.fallDistance > fallThreshold.getValue()) {
            mc.player.fallDistance = 0f;
        }
    }
}
