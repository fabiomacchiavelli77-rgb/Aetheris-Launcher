package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.ModeSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;

import java.util.ArrayList;
import java.util.List;

public class InventorySort extends Module {

    public enum SortMode {
        CATEGORY("Categoria"),
        NAME("Nome"),
        ID("ID"),
        QUANTITY("Quantità");

        private final String label;
        SortMode(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final ModeSetting<SortMode> sortMode = new ModeSetting<>("sortMode", "Sort Mode", "Modalità Ordine", SortMode.CATEGORY);
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo Operations", 1.0, 0.0, 10.0, 1.0, "ticks");
    private final BooleanSetting sortHotbar = new BooleanSetting("sortHotbar", "Sort Hotbar", "Ordina Hotbar", false);
    private final BooleanSetting sortContainer = new BooleanSetting("sortContainer", "Sort Open Container", "Ordina Contenitore Aperto", true);
    private final BooleanSetting autoSort = new BooleanSetting("autoSort", "Auto Sort", "Ordinamento Automatico", true);

    private int cooldown = 0;

    public InventorySort() {
        super("InventorySort", "Riordina automaticamente l'inventario e i contenitori aperti.", Category.PLAYER);
        addSetting(sortMode);
        addSetting(delay);
        addSetting(sortHotbar);
        addSetting(sortContainer);
        addSetting(autoSort);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (autoSort.isOn()) {
            sortInventoryStep();
        }
    }

    private void sortInventoryStep() {
        AbstractContainerMenu menu = mc.player.containerMenu;
        if (menu == null) return;

        int startSlot;
        int endSlot;

        boolean isChest = menu != mc.player.inventoryMenu;
        if (isChest && sortContainer.isOn()) {
            int containerSize = menu.slots.size() - 36;
            if (containerSize <= 0) return;
            startSlot = 0;
            endSlot = containerSize - 1;
        } else {
            startSlot = 9;
            endSlot = sortHotbar.isOn() ? 44 : 35;
        }

        if (startSlot >= endSlot || endSlot >= menu.slots.size()) return;

        // 1. Stack Merging Phase
        for (int i = startSlot; i < endSlot; i++) {
            ItemStack stackI = menu.getSlot(i).getItem();
            if (stackI.isEmpty() || stackI.getCount() >= stackI.getMaxStackSize()) continue;

            for (int j = i + 1; j <= endSlot; j++) {
                ItemStack stackJ = menu.getSlot(j).getItem();
                if (stackJ.isEmpty()) continue;

                if (ItemStack.isSameItemSameComponents(stackI, stackJ)) {
                    clickSlot(menu.containerId, j, 0, ClickType.PICKUP);
                    clickSlot(menu.containerId, i, 0, ClickType.PICKUP);
                    if (!menu.getCarried().isEmpty()) {
                        clickSlot(menu.containerId, j, 0, ClickType.PICKUP);
                    }
                    cooldown = delay.getIntValue();
                    return;
                }
            }
        }

        // 2. Build target sorted order
        List<ItemStack> stacks = new ArrayList<>();
        List<Integer> slotIndices = new ArrayList<>();
        for (int i = startSlot; i <= endSlot; i++) {
            stacks.add(menu.getSlot(i).getItem());
            slotIndices.add(i);
        }

        List<ItemStack> sorted = new ArrayList<>(stacks);
        SortMode mode = sortMode.getValue();
        sorted.sort((s1, s2) -> compareItems(s1, s2, mode));

        // 3. Find first mismatch and swap
        for (int idx = 0; idx < stacks.size(); idx++) {
            int slotI = slotIndices.get(idx);
            ItemStack currentI = menu.getSlot(slotI).getItem();
            ItemStack targetI = sorted.get(idx);

            if (isMatchingStack(currentI, targetI)) continue;

            int targetSlotJ = -1;
            for (int jIdx = idx + 1; jIdx < stacks.size(); jIdx++) {
                int slotJ = slotIndices.get(jIdx);
                ItemStack currentJ = menu.getSlot(slotJ).getItem();
                if (isMatchingStack(currentJ, targetI)) {
                    targetSlotJ = slotJ;
                    break;
                }
            }

            if (targetSlotJ != -1) {
                if (!menu.getCarried().isEmpty()) {
                    clickSlot(menu.containerId, targetSlotJ, 0, ClickType.PICKUP);
                }
                clickSlot(menu.containerId, targetSlotJ, 0, ClickType.PICKUP);
                clickSlot(menu.containerId, slotI, 0, ClickType.PICKUP);
                if (!menu.getCarried().isEmpty()) {
                    clickSlot(menu.containerId, targetSlotJ, 0, ClickType.PICKUP);
                }
                cooldown = delay.getIntValue();
                return;
            }
        }
    }

    private boolean isMatchingStack(ItemStack s1, ItemStack s2) {
        if (s1.isEmpty() && s2.isEmpty()) return true;
        if (s1.isEmpty() || s2.isEmpty()) return false;
        return ItemStack.isSameItemSameComponents(s1, s2) && s1.getCount() == s2.getCount();
    }

    private int compareItems(ItemStack s1, ItemStack s2, SortMode mode) {
        if (s1.isEmpty() && s2.isEmpty()) return 0;
        if (s1.isEmpty()) return 1;
        if (s2.isEmpty()) return -1;

        switch (mode) {
            case CATEGORY -> {
                int c1 = getItemCategory(s1);
                int c2 = getItemCategory(s2);
                if (c1 != c2) return Integer.compare(c1, c2);
                int nameComp = s1.getHoverName().getString().compareToIgnoreCase(s2.getHoverName().getString());
                if (nameComp != 0) return nameComp;
                return Integer.compare(s2.getCount(), s1.getCount());
            }
            case NAME -> {
                int nameComp = s1.getHoverName().getString().compareToIgnoreCase(s2.getHoverName().getString());
                if (nameComp != 0) return nameComp;
                return Integer.compare(s2.getCount(), s1.getCount());
            }
            case ID -> {
                String id1 = BuiltInRegistries.ITEM.getKey(s1.getItem()).toString();
                String id2 = BuiltInRegistries.ITEM.getKey(s2.getItem()).toString();
                int idComp = id1.compareToIgnoreCase(id2);
                if (idComp != 0) return idComp;
                return Integer.compare(s2.getCount(), s1.getCount());
            }
            case QUANTITY -> {
                int countComp = Integer.compare(s2.getCount(), s1.getCount());
                if (countComp != 0) return countComp;
                return s1.getHoverName().getString().compareToIgnoreCase(s2.getHoverName().getString());
            }
        }
        return 0;
    }

    private int getItemCategory(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.is(net.minecraft.tags.ItemTags.SWORDS) || (stack.is(net.minecraft.world.item.Items.BOW) || stack.is(net.minecraft.tags.ItemTags.BOW_ENCHANTABLE)) || item == Items.CROSSBOW || item == Items.TRIDENT) {
            return 0; // Weapons
        }
        if (stack.is(net.minecraft.tags.ItemTags.PICKAXES) || stack.is(net.minecraft.tags.ItemTags.AXES) || stack.is(net.minecraft.tags.ItemTags.SHOVELS) || stack.is(net.minecraft.tags.ItemTags.HOES) || item == Items.SHEARS || item == Items.FISHING_ROD) {
            return 1; // Tools
        }
        if (stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE) || item == Items.SHIELD || item == Items.ELYTRA) {
            return 2; // Armor
        }
        if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.TOTEM_OF_UNDYING || item == Items.ENDER_PEARL || item == Items.EXPERIENCE_BOTTLE || item instanceof PotionItem) {
            return 3; // Utility
        }
        if (stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
            return 4; // Food
        }
        if (item == Items.DIAMOND || item == Items.NETHERITE_INGOT || item == Items.GOLD_INGOT || item == Items.IRON_INGOT || item == Items.EMERALD || item == Items.LAPIS_LAZULI || item == Items.REDSTONE || item == Items.COAL || item == Items.AMETHYST_SHARD) {
            return 5; // Minerals
        }
        if (item instanceof BlockItem) {
            return 6; // Blocks
        }
        return 7; // Misc
    }

    private void clickSlot(int containerId, int slotId, int button, ClickType clickType) {
        if (mc.gameMode != null && mc.player != null) {
            mc.gameMode.handleInventoryMouseClick(containerId, slotId, button, clickType, mc.player);
        }
    }
}
