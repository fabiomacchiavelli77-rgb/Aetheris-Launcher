package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class FastPlace extends Module {
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo", 0.0, 0.0, 4.0, 1.0, "ticks");

    public FastPlace() {
        super("FastPlace", "Rimuove il cooldown tra un piazzamento e l'altro.", Category.PLAYER);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Resetta il cooldown di utilizzo oggetti
        if (mc.player.getUseItemRemainingTicks() > delay.getValue().intValue()) {
            mc.player.getUseItemRemainingTicks();
        }
    }
}
