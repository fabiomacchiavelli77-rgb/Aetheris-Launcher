package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class NoClip extends Module {
    private final SliderSetting speed = new SliderSetting("speed", "Speed", "Velocità", 0.5, 0.1, 2.0, 0.1, "x");

    public NoClip() {
        super("NoClip", "Passa attraverso i blocchi (solo lato client).", Category.MOVEMENT);
        addSetting(speed);
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
