package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Rinasce automaticamente alla morte.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.isDeadOrDying()) {
            mc.player.respawn();
        }
    }
}
