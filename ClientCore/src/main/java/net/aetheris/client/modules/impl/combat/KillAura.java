package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;

public class KillAura extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 3.8, 1.0, 6.0, 0.1, "blocks");
    private final SliderSetting cps = new SliderSetting("cps", "Attack Speed", "Velocità Attacco", 12.0, 1.0, 20.0, 0.5, "CPS");
    private final BooleanSetting cooldownSync = new BooleanSetting("cooldownSync", "Cooldown Sync", "Sinc. Cooldown", true);
    private final BooleanSetting targetPlayers = new BooleanSetting("targetPlayers", "Target Players", "Bersaglia Giocatori", true);
    private final BooleanSetting targetMobs = new BooleanSetting("targetMobs", "Target Mobs", "Bersaglia Mob", true);
    private final BooleanSetting targetAnimals = new BooleanSetting("targetAnimals", "Target Animals", "Bersaglia Animali", false);
    private final SliderSetting fov = new SliderSetting("fov", "FOV Angle", "Angolo FOV", 360, 0, 360, 5, "°");

    private int attackCooldown = 0;

    public KillAura() {
        super("KillAura", "Attacca automaticamente le entità vicine.", Category.COMBAT);
        addSetting(range);
        addSetting(cps);
        addSetting(cooldownSync);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetAnimals);
        addSetting(fov);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        
        if (cooldownSync.isOn()) {
            if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;
        } else {
            if (attackCooldown > 0) { attackCooldown--; return; }
        }

        Entity target = findTarget();
        if (target == null) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        
        if (!cooldownSync.isOn()) {
            attackCooldown = (int) (20.0 / cps.getValue());
        }
    }

    private Entity findTarget() {
        Entity best = null;
        double bestDist = range.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!isValidTarget(entity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist < bestDist) { bestDist = dist; best = entity; }
        }
        return best;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player) return false;
        if (!entity.isAlive()) return false;
        if (entity.distanceTo(mc.player) > range.getValue()) return false;
        if (!(entity instanceof LivingEntity)) return false;

        boolean isPlayer = entity instanceof Player;
        boolean isMob = entity instanceof Enemy;
        boolean isAnimal = entity instanceof Animal;

        if (isPlayer && !targetPlayers.isOn()) return false;
        if (isMob && !targetMobs.isOn()) return false;
        if (isAnimal && !targetAnimals.isOn()) return false;
        if (!isPlayer && !isMob && !isAnimal && !targetMobs.isOn()) return false;

        if (fov.getValue() < 360) {
            double diffX = entity.getX() - mc.player.getX();
            double diffZ = entity.getZ() - mc.player.getZ();
            float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
            float yawDiff = Mth.wrapDegrees(yaw - mc.player.getYRot());
            if (Math.abs(yawDiff) > fov.getValue() / 2.0) {
                return false;
            }
        }

        return true;
    }
}
