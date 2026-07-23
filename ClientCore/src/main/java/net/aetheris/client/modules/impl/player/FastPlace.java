package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "Rimuove il cooldown tra un piazzamento e l'altro.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Resetta il cooldown di utilizzo oggetti
        if (mc.player.getUseItemRemainingTicks() > 0) {
            mc.player.getUseItemRemainingTicks();
        }
    }
}
