package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.network.chat.Component;

/**
 * PermissionViewer — shows client-side profile information in chat.
 * Does NOT touch anything server-side: no commands sent, no packets modified.
 */
public class PermissionViewer extends Module {

    public PermissionViewer() {
        super("PermissionViewer", "Shows your client profile info in chat (client-side only)", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        String userName = mc.getUser().getName();
        String serverAddr = mc.getCurrentServer() != null ? mc.getCurrentServer().ip : "Singleplayer";

        mc.player.displayClientMessage(
                Component.literal("§a[Aetheris] §7PermissionViewer §aON"), false);
        mc.player.displayClientMessage(
                Component.literal("§7User: §f" + userName + " §7| Server: §f" + serverAddr), false);
        mc.player.displayClientMessage(
                Component.literal("§7Note: §8Server-side permissions (LuckPerms, PEX) are not accessible from the client."), false);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[Aetheris] PermissionViewer §cOFF"), false);
        }
    }
}