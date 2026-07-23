package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class NoClip extends Module {

    public NoClip() {
        super("NoClip", "Passa attraverso i blocchi (solo lato client).", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Disabilita la collisione con i blocchi
        mc.player.noPhysics = true;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noPhysics = false;
        }
    }
}
