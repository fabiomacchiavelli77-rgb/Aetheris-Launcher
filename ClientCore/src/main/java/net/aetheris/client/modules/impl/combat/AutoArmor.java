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
            if (stack.isEmpty() || !stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE)) continue;

            int targetSlot = switch (stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot().getIndex()) {
                case 5 -> 39; // Head -> helmet slot
                case 4 -> 38; // Chest -> chestplate slot
                case 3 -> 37; // Legs -> leggings slot
                case 2 -> 36; // Feet -> boots slot
                default -> -1;
            };
            if (targetSlot == -1) continue;

            ItemStack equipped = mc.player.getInventory().getArmor(targetSlot - 36);
            if (equipped.isEmpty() || isBetter(stack, equipped)) {
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

    private boolean isBetter(ItemStack newStack, ItemStack oldStack) {
        return getDefense(newStack) > getDefense(oldStack);
    }

    private double getDefense(ItemStack stack) {
        double def = 0;
        var modifiers = stack.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).modifiers();
        for (var entry : modifiers) {
            if (entry.attribute() == net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) {
                def += entry.modifier().amount();
            }
        }
        return def;
    }
}
