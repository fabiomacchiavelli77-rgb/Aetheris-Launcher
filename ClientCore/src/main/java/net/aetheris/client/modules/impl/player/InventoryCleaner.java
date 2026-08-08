package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Set;
import net.aetheris.client.settings.SliderSetting;
import net.aetheris.client.settings.BooleanSetting;

public class InventoryCleaner extends Module {
    private final SliderSetting dropDelay = new SliderSetting("dropDelay", "Drop Delay", "Ritardo Scarto", 2.0, 0.0, 20.0, 1.0, "ticks");
    private final BooleanSetting keepEquipment = new BooleanSetting("keepEquipment", "Keep Equipment", "Mantieni Equipaggiamento", true);
    
    private int cleanDelay = 0;
    private static final Set<Class<?>> JUNK = Set.of(
        // Blocchi comuni da buttare
        BlockItem.class
    );

    public InventoryCleaner() {
        super("InventoryCleaner", "Butta automaticamente gli oggetti inutili.", Category.PLAYER);
        addSetting(dropDelay);
        addSetting(keepEquipment);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (cleanDelay > 0) { cleanDelay--; return; }

        for (int i = 9; i < 36; i++) { // Solo inventario (non hotbar)
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Tieni armi, armature, tool, cibo, minerali
            if (keepEquipment.isOn() && isValuable(stack)) continue;

            // Butta il resto
            mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                i,
                0,
                net.minecraft.world.inventory.ClickType.THROW,
                mc.player
            );
            cleanDelay = dropDelay.getValue().intValue();
            return;
        }
        cleanDelay = 20;
    }

    private boolean isValuable(ItemStack stack) {
        Item item = stack.getItem();
        return stack.is(net.minecraft.tags.ItemTags.SWORDS) ||
               stack.is(net.minecraft.tags.ItemTags.PICKAXES) ||
               stack.is(net.minecraft.tags.ItemTags.AXES) ||
               stack.is(net.minecraft.tags.ItemTags.SHOVELS) ||
               stack.is(net.minecraft.tags.ItemTags.HOES) ||
               (stack.is(net.minecraft.world.item.Items.BOW) || stack.is(net.minecraft.tags.ItemTags.BOW_ENCHANTABLE)) ||
               stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE) ||
               item == Items.CROSSBOW ||
               item == Items.TRIDENT ||
               item == Items.SHIELD ||
               item == Items.FISHING_ROD ||
               item == Items.TOTEM_OF_UNDYING ||
               item == Items.DIAMOND ||
               item == Items.NETHERITE_INGOT ||
               item == Items.NETHERITE_SCRAP ||
               item == Items.ANCIENT_DEBRIS ||
               stack.has(net.minecraft.core.component.DataComponents.FOOD);
    }
}
