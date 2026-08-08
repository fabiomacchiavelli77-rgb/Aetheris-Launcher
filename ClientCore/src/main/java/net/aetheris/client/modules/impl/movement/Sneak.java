package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class Sneak extends Module {

    public Sneak() {
        super("Sneak", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(false);
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(true);
        }
    }
}
