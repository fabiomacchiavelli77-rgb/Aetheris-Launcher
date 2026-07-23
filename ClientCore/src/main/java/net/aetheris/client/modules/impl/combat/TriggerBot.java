package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    private int attackDelay = 0;

    public TriggerBot() {
        super("TriggerBot", "Attacca quando il mirino è su un'entità.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.hitResult == null) return;
        if (attackDelay > 0) { attackDelay--; return; }
        if (!(mc.hitResult instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;
        if (!target.isAlive()) return;
        if (target == mc.player) return;
        if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        attackDelay = 10; // ~2 colpi al secondo
    }
}
