package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.ModeSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFall extends Module {
    public enum Mode { PACKET }

    private final ModeSetting<Mode> mode = new ModeSetting<>("mode", "Mode", "Modalità", Mode.PACKET);
    private final SliderSetting fallThreshold = new SliderSetting("fallThreshold", "Fall Distance", "Distanza Caduta", 2.5, 1.5, 5.0, 0.5, "blocks");

    public NoFall() {
        super("NoFall", "Previene il danno da caduta inviando pacchetti di atterraggio falsi al server.", Category.MOVEMENT);
        addSetting(mode);
        addSetting(fallThreshold);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;

        if (mc.player.fallDistance > fallThreshold.getValue()) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
            mc.player.fallDistance = 0f;
        }
    }
}
