package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player != null && !mc.player.horizontalCollision && mc.player.getSpeed() > 0) {
            mc.player.setSprinting(true);
        }
    }
}
