package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Previene il danno da caduta.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.fallDistance > 2.5f) {
            mc.player.fallDistance = 0f;
        }
    }
}
