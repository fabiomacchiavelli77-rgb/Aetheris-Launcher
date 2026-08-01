package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.aetheris.client.settings.BooleanSetting;

public class Fly extends Module {
    private final SliderSetting hSpeed = new SliderSetting("hSpeed", "H. Speed", "Velocità Orizz.", 1.0, 0.1, 5.0, 0.1, "x");
    private final SliderSetting vSpeed = new SliderSetting("vSpeed", "V. Speed", "Velocità Vert.", 0.5, 0.1, 5.0, 0.1, "x");
    private final BooleanSetting antiKick = new BooleanSetting("antiKick", "Anti-Kick", "Anti-Kick", true);

    public Fly() {
        super("Fly", "Permette di volare in sopravvivenza.", Category.MOVEMENT);
        addSetting(hSpeed);
        addSetting(vSpeed);
        addSetting(antiKick);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        mc.player.getAbilities().mayfly = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().mayfly = false;
            mc.player.getAbilities().flying = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.getAbilities().mayfly = true;
        if (mc.player.getAbilities().flying) {
            double hSpd = mc.player.isSprinting() ? hSpeed.getValue() * 1.5 : hSpeed.getValue();
            
            if (mc.options.keyJump.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, vSpeed.getValue() * 0.5, 0));
            if (mc.options.keyShift.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(0, -vSpeed.getValue() * 0.5, 0));
            if (!mc.options.keyJump.isDown() && !mc.options.keyShift.isDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0, mc.player.getDeltaMovement().z);
                
            // Anti-kick logic could go here if implemented in the future
        }
    }
}
