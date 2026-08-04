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
        if (mc.player != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null && mc.options.keyShift != null) {
            mc.options.keyShift.setDown(true);
        }
    }
}
