package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;

public class AutoTotem extends Module {
    private int checkDelay = 0;

    public AutoTotem() {
        super("AutoTotem", "Tiene automaticamente un Totem della Non-morte nella mano secondaria.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (checkDelay > 0) { checkDelay--; return; }
        checkDelay = 10; // Check ogni 10 tick

        // Se la mano secondaria è vuota o non ha un totem
        if (mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
            // Cerca un totem nell'inventario
            for (int i = 0; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    // Sposta il totem nella mano secondaria
                    mc.player.getInventory().selected = i < 9 ? i : mc.player.getInventory().selected;
                    mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, net.minecraft.core.BlockPos.ZERO, net.minecraft.core.Direction.DOWN));
                    checkDelay = 20;
                    return;
                }
            }
        }
    }
}
