package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class StrongholdFinder extends Module {

    private final BooleanSetting autoReset = new BooleanSetting("autoReset", "Auto Reset", "Resetta automaticamente dopo calcolo", false);

    private final List<Ray> eyeRays = new ArrayList<>();

    public StrongholdFinder() {
        super("StrongholdFinder", "Triangola la traiettoria degli Eye of Ender per calcolare le coordinate X, Z della Stronghold.", Category.WORLD);
        addSetting(autoReset);
    }

    @Override
    public void onEnable() {
        eyeRays.clear();
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        for (var entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EyeOfEnder eye) {
                Vec3 pos = eye.position();
                Vec3 vel = eye.getDeltaMovement();

                // L'occhio deve avere una velocità orizzontale significativa per determinare l'angolo
                double horizontalSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                if (horizontalSpeed > 0.01) {
                    double yaw = Math.atan2(vel.z, vel.x);

                    // Evita di registrare due volte lo stesso lancio di Occhio
                    if (isNewRay(pos, yaw)) {
                        Ray ray = new Ray(pos.x, pos.z, yaw);
                        eyeRays.add(ray);

                        mc.player.displayClientMessage(Component.literal("§a[StrongholdFinder] §7Registrata traiettoria Eye #" + eyeRays.size() + " (" + String.format("%.1f, %.1f", pos.x, pos.z) + ")"), false);

                        if (eyeRays.size() >= 2) {
                            calculateStronghold();
                        }
                    }
                }
            }
        }
    }

    private boolean isNewRay(Vec3 pos, double yaw) {
        for (Ray r : eyeRays) {
            double distSq = (r.x - pos.x) * (r.x - pos.x) + (r.z - pos.z) * (r.z - pos.z);
            if (distSq < 100.0) return false; // troppo vicino a un punto già registrato
        }
        return true;
    }

    private void calculateStronghold() {
        Ray r1 = eyeRays.get(0);
        Ray r2 = eyeRays.get(1);

        // Triangolazione rette 2D: (x - x1) * tan(yaw1) = z - z1
        // m1 = tan(yaw1), m2 = tan(yaw2)
        // x1, z1 e x2, z2
        // z - m1*x = z1 - m1*x1
        // z - m2*x = z2 - m2*x2

        double m1 = Math.tan(r1.yaw);
        double m2 = Math.tan(r2.yaw);

        if (Math.abs(m1 - m2) < 0.001) {
            mc.player.displayClientMessage(Component.literal("§c[StrongholdFinder] Traiettorie parallele! Lanciane un altro da una posizione più distante."), false);
            return;
        }

        // x * (m1 - m2) = (r1.z - m1 * r1.x) - (r2.z - m2 * r2.x)
        double c1 = r1.z - m1 * r1.x;
        double c2 = r2.z - m2 * r2.x;

        double targetX = (c2 - c1) / (m1 - m2);
        double targetZ = m1 * targetX + c1;

        mc.player.displayClientMessage(Component.literal("§a[StrongholdFinder] §eStronghold trovata alle coordinate X: " + (int) Math.round(targetX) + " | Z: " + (int) Math.round(targetZ)), false);

        if (autoReset.isOn()) {
            eyeRays.clear();
        }
    }

    private static class Ray {
        final double x, z;
        final double yaw; // in radianti

        Ray(double x, double z, double yaw) {
            this.x = x;
            this.z = z;
            this.yaw = yaw;
        }
    }
}
