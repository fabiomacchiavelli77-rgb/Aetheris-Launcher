package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.phys.Vec3;

public class Speed extends Module {
    private static final double SPEED_BOOST = 1.8;

    public Speed() {
        super("Speed", "Aumenta la velocità di movimento.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!isMoving()) return;
        if (!mc.player.onGround()) return;

        Vec3 delta = mc.player.getDeltaMovement();
        double yaw = Math.toRadians(mc.player.getYRot());

        float forward = mc.player.input.forwardImpulse;
        float sideways = mc.player.input.leftImpulse;

        double x = -Math.sin(yaw) * forward - Math.cos(yaw) * sideways;
        double z = Math.cos(yaw) * forward - Math.sin(yaw) * sideways;

        double len = Math.sqrt(x * x + z * z);
        if (len > 0) {
            x = x / len * SPEED_BOOST * 0.2;
            z = z / len * SPEED_BOOST * 0.2;
            mc.player.setDeltaMovement(x, delta.y, z);
        }
        if (mc.player.horizontalCollision) mc.player.jumpFromGround();
    }

    private boolean isMoving() {
        return mc.player != null &&
               (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0);
    }
}
