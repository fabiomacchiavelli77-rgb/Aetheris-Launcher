package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 4.5, 1.0, 8.0, 0.5, "blocks");
    private final SliderSetting speed = new SliderSetting("speed", "Speed", "Velocità", 0.5, 0.05, 2.0, 0.05, "");
    private final SliderSetting fov = new SliderSetting("fov", "FOV", "Angolo Visivo", 60, 0, 180, 5, "°");
    private final BooleanSetting targetPlayers = new BooleanSetting("targetPlayers", "Target Players", "Bersaglia Giocatori", true);

    public AimAssist() {
        super("AimAssist", "Ruota gradualmente il mirino verso le entità vicine.", Category.COMBAT);
        addSetting(range);
        addSetting(speed);
        addSetting(fov);
        addSetting(targetPlayers);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isUsingItem()) return;

        Entity target = findTarget();
        if (target == null) return;

        Vec3 targetPos = target.getEyePosition();
        Vec3 eye = mc.player.getEyePosition();
        double dx = targetPos.x - eye.x;
        double dy = targetPos.y - eye.y;
        double dz = targetPos.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.001) return;

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        float yawDiff = Mth.wrapDegrees(targetYaw - mc.player.getYRot());
        float pitchDiff = Mth.wrapDegrees(targetPitch - mc.player.getXRot());
        float step = speed.getValue().floatValue() * 5.0f;

        mc.player.setYRot(mc.player.getYRot() + Math.copySign(Math.min(Math.abs(yawDiff), step), yawDiff));
        mc.player.setXRot(mc.player.getXRot() + Math.copySign(Math.min(Math.abs(pitchDiff), step), pitchDiff));
    }

    private Entity findTarget() {
        Entity best = null;
        double bestDist = range.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive()) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living instanceof Player && !targetPlayers.isOn()) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > bestDist) continue;

            double diffX = entity.getX() - mc.player.getX();
            double diffZ = entity.getZ() - mc.player.getZ();
            float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
            if (Math.abs(Mth.wrapDegrees(yaw - mc.player.getYRot())) > fov.getValue() / 2.0) continue;

            bestDist = dist;
            best = entity;
        }
        return best;
    }
}
