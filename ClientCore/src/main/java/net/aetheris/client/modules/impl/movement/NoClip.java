package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.phys.Vec3;

public class NoClip extends Module {
    private final SliderSetting speed = new SliderSetting("speed", "Speed", "Velocità di movimento (0.1x - 3.0x)", 0.5, 0.1, 3.0, 0.1, "x");

    public NoClip() {
        super("NoClip", "Passa attraverso i blocchi e vola liberamente. Usa W/A/S/D per muoverti, Spazio per salire, Shift per scendere.", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Forza noPhysics ad ogni tick
        mc.player.noPhysics = true;

        double moveSpeed = speed.getValue();

        // Movimento 3D controllato in base alla direzione dello sguardo e ai tasti premuti
        double yaw = Math.toRadians(mc.player.getYRot());
        double forward = (mc.player.input.keyPresses.forward() ? 1 : 0) - (mc.player.input.keyPresses.backward() ? 1 : 0);
        double side = (mc.player.input.keyPresses.left() ? 1 : 0) - (mc.player.input.keyPresses.right() ? 1 : 0);

        double dx = (forward * -Math.sin(yaw) + side * Math.cos(yaw)) * moveSpeed;
        double dz = (forward * Math.cos(yaw) + side * Math.sin(yaw)) * moveSpeed;
        double dy = 0;

        if (mc.options.keyJump.isDown()) {
            dy += moveSpeed;
        }
        if (mc.options.keyShift.isDown()) {
            dy -= moveSpeed;
        }

        mc.player.setDeltaMovement(new Vec3(dx, dy, dz));
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.noPhysics = false;
        }
    }
}
