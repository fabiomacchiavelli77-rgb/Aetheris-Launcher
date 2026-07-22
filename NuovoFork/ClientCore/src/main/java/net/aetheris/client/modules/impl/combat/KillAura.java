package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;

public class KillAura extends Module {
    private static final double RANGE = 4.5;
    private int attackCooldown = 0;
    private static final int COOLDOWN_TICKS = 8;

    public KillAura() {
        super("KillAura", "Attacca automaticamente le entità vicine.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (attackCooldown > 0) { attackCooldown--; return; }
        if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        Entity target = findTarget();
        if (target == null) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        attackCooldown = COOLDOWN_TICKS;
    }

    private Entity findTarget() {
        Entity best = null;
        double bestDist = RANGE;
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
        if (entity.distanceTo(mc.player) > RANGE) return false;
        return entity instanceof LivingEntity;
    }
}
