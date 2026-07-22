package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class AutoArmor extends Module {
    private int equipDelay = 0;

    public AutoArmor() {
        super("AutoArmor", "Equipaggia automaticamente la migliore armatura.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (equipDelay > 0) { equipDelay--; return; }

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) continue;

            int targetSlot = switch (armor.getEquipmentSlot().getIndex()) {
                case 5 -> 39; // Head -> helmet slot
                case 4 -> 38; // Chest -> chestplate slot
                case 3 -> 37; // Legs -> leggings slot
                case 2 -> 36; // Feet -> boots slot
                default -> -1;
            };
            if (targetSlot == -1) continue;

            ItemStack equipped = mc.player.getInventory().getArmor(targetSlot - 36);
            if (equipped.isEmpty() || isBetter(armor, stack, equipped)) {
                mc.gameMode.handleInventoryMouseClick(
                    mc.player.containerMenu.containerId,
                    slot < 9 ? slot + 36 : slot,
                    0,
                    net.minecraft.world.inventory.ClickType.QUICK_MOVE,
                    mc.player
                );
                equipDelay = 5;
                return;
            }
        }
    }

    private boolean isBetter(ArmorItem newArmor, ItemStack newStack, ItemStack oldStack) {
        if (!(oldStack.getItem() instanceof ArmorItem oldArmor)) return true;
        return newArmor.getDefense() > oldArmor.getDefense();
    }
}
