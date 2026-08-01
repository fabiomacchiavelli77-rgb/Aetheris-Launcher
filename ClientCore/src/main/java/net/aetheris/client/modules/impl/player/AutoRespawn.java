package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class AutoRespawn extends Module {
    private final SliderSetting delay = new SliderSetting("delay", "Respawn Delay", "Ritardo Respawn", 0.0, 0.0, 100.0, 5.0, "ticks");
    private int ticksDead = 0;

    public AutoRespawn() {
        super("AutoRespawn", "Rinasce automaticamente alla morte.", Category.PLAYER);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.isDeadOrDying()) {
            ticksDead++;
            if (ticksDead >= delay.getValue().intValue()) {
                mc.player.respawn();
            }
        } else {
            ticksDead = 0;
        }
    }
}
