package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class Fly extends Module {
    private static final double FLY_SPEED = 0.6;
    private static final double FAST_FLY_SPEED = 1.2;

    public Fly() {
        super("Fly", "Permette di volare in sopravvivenza.", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        mc.player.getAbilities().mayfly = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.getAbilities().mayfly = true;
        if (mc.player.getAbilities().flying) {
            double speed = mc.player.isSprinting() ? FAST_FLY_SPEED : FLY_SPEED;
            if (mc.options.keyJump.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, speed * 0.5, 0));
            if (mc.options.keyShift.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, -speed * 0.5, 0));
            if (!mc.options.keyJump.isDown() && !mc.options.keyShift.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
        }
    }
}
