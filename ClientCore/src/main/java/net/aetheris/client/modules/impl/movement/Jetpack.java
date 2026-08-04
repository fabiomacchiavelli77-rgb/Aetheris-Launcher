package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class Jetpack extends Module {
    private final SliderSetting speed = new SliderSetting("speed", "Speed", "Velocità di salita", 0.42, 0.1, 1.5, 0.05);

    public Jetpack() {
        super("Jetpack", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.level != null) {
            if (mc.options.keyJump.isDown()) {
                Vec3 vel = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(vel.x, speed.getValue(), vel.z);

                if (mc.level.isClientSide()) {
                    double px = mc.player.getX();
                    double py = mc.player.getY();
                    double pz = mc.player.getZ();
                    mc.level.addParticle(ParticleTypes.FLAME, px, py, pz, 0.0, -0.2, 0.0);
                    mc.level.addParticle(ParticleTypes.SMOKE, px, py, pz, 0.0, -0.2, 0.0);
                }
            }
        }
    }
}
