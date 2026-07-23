package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class Step extends Module {
    private static final float STEP_HEIGHT = 2.0f;
    private float oldStepHeight;

    public Step() {
        super("Step", "Sale automaticamente blocchi fino a 2 blocchi.", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            oldStepHeight = mc.player.maxUpStep();
            mc.player.setMaxUpStep(STEP_HEIGHT);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setMaxUpStep(oldStepHeight);
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.maxUpStep() != STEP_HEIGHT) {
            mc.player.setMaxUpStep(STEP_HEIGHT);
        }
    }
}
