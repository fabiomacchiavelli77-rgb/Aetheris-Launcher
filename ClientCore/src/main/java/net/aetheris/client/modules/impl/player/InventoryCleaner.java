package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.*;
import java.util.Set;

public class InventoryCleaner extends Module {
    private int cleanDelay = 0;
    private static final Set<Class<?>> JUNK = Set.of(
        // Blocchi comuni da buttare
        BlockItem.class
    );

    public InventoryCleaner() {
        super("InventoryCleaner", "Butta automaticamente gli oggetti inutili.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (cleanDelay > 0) { cleanDelay--; return; }

        for (int i = 9; i < 36; i++) { // Solo inventario (non hotbar)
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            // Tieni armi, armature, tool, cibo, minerali
            if (isValuable(item)) continue;

            // Butta il resto
            mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                i,
                0,
                net.minecraft.world.inventory.ClickType.THROW,
                mc.player
            );
            cleanDelay = 5;
            return;
        }
        cleanDelay = 20;
    }

    private boolean isValuable(Item item) {
        return item instanceof SwordItem ||
               item instanceof PickaxeItem ||
               item instanceof AxeItem ||
               item instanceof ShovelItem ||
               item instanceof HoeItem ||
               item instanceof ArmorItem ||
               item instanceof BowItem ||
               item instanceof CrossbowItem ||
               item instanceof TridentItem ||
               item instanceof ShieldItem ||
               item instanceof FishingRodItem ||
               item == Items.TOTEM_OF_UNDYING ||
               item == Items.DIAMOND ||
               item == Items.NETHERITE_INGOT ||
               item == Items.NETHERITE_SCRAP ||
               item == Items.ANCIENT_DEBRIS ||
               item == Items.GOLDEN_APPLE ||
               item == Items.ENCHANTED_GOLDEN_APPLE ||
               item == Items.ENDER_PEARL ||
               item == Items.OBSIDIAN ||
               item == Items.CRYING_OBSIDIAN;
    }
}
