package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;

public class AntiAFK extends Module {
    private final SliderSetting interval = new SliderSetting("interval", "Interval", "Intervallo", 5.0, 1.0, 60.0, 1.0, "s");
    private final BooleanSetting rotate = new BooleanSetting("rotate", "Rotate", "Ruota", true);
    private final BooleanSetting swing = new BooleanSetting("swing", "Swing", "Oscilla", false);

    private int timer = 0;

    public AntiAFK() {
        super("AntiAFK", "Esegue piccoli movimenti periodici per evitare il kick per inattività.", Category.PLAYER);
        addSetting(interval);
        addSetting(rotate);
        addSetting(swing);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        timer++;
        if (timer < interval.getIntValue() * 20) return;
        timer = 0;

        if (rotate.isOn()) {
            mc.player.setYRot(mc.player.getYRot() + 15.0f);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
        }
        if (swing.isOn()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
