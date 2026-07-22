package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;

public class ChestStealer extends Module {
    private int stealDelay = 0;

    public ChestStealer() {
        super("ChestStealer", "Ruba automaticamente il contenuto delle chest.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (stealDelay > 0) { stealDelay--; return; }

        if (mc.player.containerMenu instanceof ChestMenu chest) {
            boolean stole = false;
            for (int slot = 0; slot < chest.getContainer().getContainerSize(); slot++) {
                ItemStack stack = chest.getContainer().getItem(slot);
                if (!stack.isEmpty()) {
                    mc.gameMode.handleInventoryMouseClick(
                        chest.containerId,
                        slot,
                        0,
                        net.minecraft.world.inventory.ClickType.QUICK_MOVE,
                        mc.player
                    );
                    stole = true;
                    break;
                }
            }
            stealDelay = stole ? 3 : 10;
        }
    }
}
