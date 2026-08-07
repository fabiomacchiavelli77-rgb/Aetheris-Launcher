package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class PacketLogger extends Module {

    private final BooleanSetting logInbound = new BooleanSetting("logInbound", "Log Inbound", "Registra pacchetti in arrivo", true);
    private final BooleanSetting logOutbound = new BooleanSetting("logOutbound", "Log Outbound", "Registra pacchetti in uscita", true);
    private final BooleanSetting cancelMove = new BooleanSetting("ignoreMove", "Ignore Move Packets", "Ignora pacchetti di movimento", true);

    public PacketLogger() {
        super("PacketLogger", "Registra i pacchetti di rete in arrivo e in uscita per debugging.", Category.WORLD);
        addSetting(logInbound);
        addSetting(logOutbound);
        addSetting(cancelMove);
    }

    public void onSendPacket(Packet<?> packet) {
        if (!isEnabled() || !logOutbound.isOn()) return;
        if (cancelMove.isOn() && packet.getClass().getSimpleName().contains("Move")) return;

        logPacket("OUTBOUND", packet);
    }

    public void onReceivePacket(Packet<?> packet) {
        if (!isEnabled() || !logInbound.isOn()) return;
        if (cancelMove.isOn() && packet.getClass().getSimpleName().contains("Move")) return;

        logPacket("INBOUND", packet);
    }

    private void logPacket(String direction, Packet<?> packet) {
        if (mc.player != null) {
            String name = packet.getClass().getSimpleName();
            mc.player.displayClientMessage(Component.literal("§8[§bPacketLogger§8] §7[" + direction + "] §f" + name), false);
        }
    }
}
