package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Module {
    private final SliderSetting triggerRange = new SliderSetting("triggerRange", "Range", "Portata", 4.0, 1.0, 6.0, 0.1, "blocks");
    private final BooleanSetting cooldownSync = new BooleanSetting("cooldownSync", "Cooldown Sync", "Sinc. Cooldown", true);
    private final BooleanSetting weaponOnly = new BooleanSetting("weaponOnly", "Weapon Only", "Solo Armi", true);

    private int attackDelay = 0;

    public TriggerBot() {
        super("TriggerBot", "Attacca quando il mirino è su un'entità.", Category.COMBAT);
        addSetting(triggerRange);
        addSetting(cooldownSync);
        addSetting(weaponOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.hitResult == null) return;
        
        if (weaponOnly.isOn()) {
            boolean isWeapon = mc.player.getMainHandItem().getItem() instanceof SwordItem || mc.player.getMainHandItem().getItem() instanceof AxeItem;
            if (!isWeapon) return;
        }

        if (cooldownSync.isOn()) {
            if (mc.player.getAttackStrengthScale(0.5f) < 1.0f) return;
        } else {
            if (attackDelay > 0) { attackDelay--; return; }
        }

        if (!(mc.hitResult instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;
        if (!target.isAlive()) return;
        if (target == mc.player) return;
        
        if (mc.player.distanceTo(target) > triggerRange.getValue()) return;

        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        
        if (!cooldownSync.isOn()) {
            attackDelay = 10; // ~2 colpi al secondo
        }
    }
}
