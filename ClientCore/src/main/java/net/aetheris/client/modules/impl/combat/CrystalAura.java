package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.phys.Vec3;

public class CrystalAura extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 5.0, 1.0, 8.0, 0.5, "blocks");
    private final SliderSetting cps = new SliderSetting("cps", "Attack Speed", "Velocità Attacco", 8.0, 1.0, 20.0, 0.5, "CPS");
    private final BooleanSetting cooldownSync = new BooleanSetting("cooldownSync", "Cooldown Sync", "Sinc. Cooldown", true);
    private final BooleanSetting rotate = new BooleanSetting("rotate", "Rotate", "Ruota", true);

    private int attackCooldown = 0;

    public CrystalAura() {
        super("CrystalAura", "Attacca automaticamente gli End Crystal vicini per esploderli.", Category.COMBAT);
        addSetting(range);
        addSetting(cps);
        addSetting(cooldownSync);
        addSetting(rotate);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (cooldownSync.isOn()) {
            if (mc.player.getAttackStrengthScale(0.5f) < 0.9f) return;
        } else {
            if (attackCooldown > 0) { attackCooldown--; return; }
        }

        EndCrystal crystal = findCrystal();
        if (crystal == null) return;

        if (rotate.isOn()) {
            lookAt(crystal.position());
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
        }

        mc.gameMode.attack(mc.player, crystal);
        mc.player.swing(InteractionHand.MAIN_HAND);

        if (!cooldownSync.isOn()) {
            attackCooldown = (int) (20.0 / cps.getValue());
        }
    }

    private EndCrystal findCrystal() {
        EndCrystal best = null;
        double bestDist = range.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) continue;
            if (!crystal.isAlive()) continue;
            double dist = mc.player.distanceTo(crystal);
            if (dist < bestDist) {
                bestDist = dist;
                best = crystal;
            }
        }
        return best;
    }

    private void lookAt(Vec3 target) {
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.001) return;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
