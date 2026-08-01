package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.ModeSetting;
import net.minecraft.world.entity.Entity;

public class Criticals extends Module {

    public enum CritMode { PACKET, JUMP, MINI_JUMP }

    private final ModeSetting<CritMode> mode = new ModeSetting<>("mode", "Mode", "Modalità", CritMode.PACKET);

    public Criticals() {
        super("Criticals", "Forza colpi critici ad ogni attacco.", Category.COMBAT);
        addSetting(mode);
    }

    public boolean shouldForceCritical(Entity target) {
        if (!isEnabled()) return false;
        if (mc.player == null) return false;
        if (mc.player.isInWater() || mc.player.isInLava()) return false;
        if (mc.player.onClimbable()) return false;
        if (mc.player.isPassenger()) return false;
        return mc.player.getAttackStrengthScale(0.5f) >= 0.9f;
    }
    
    public CritMode getMode() {
        return mode.getValue();
    }
}
