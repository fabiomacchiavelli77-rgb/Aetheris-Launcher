package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoBrewer extends Module {
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo", 3.0, 0.0, 20.0, 1.0, "ticks");
    private final BooleanSetting autoFuel = new BooleanSetting("autoFuel", "Auto Fuel", "Ricarica Blaze Powder", true);
    private final BooleanSetting autoClose = new BooleanSetting("autoClose", "Auto Close", "Chiudi quando completo", false);

    private int tickTimer = 0;

    public AutoBrewer() {
        super("AutoBrewer", "Aiuto per alchimia/alambicco automatico.", Category.WORLD);
        addSetting(delay);
        addSetting(autoFuel);
        addSetting(autoClose);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (tickTimer > 0) {
            tickTimer--;
            return;
        }

        if (mc.player.containerMenu instanceof BrewingStandMenu brewingMenu) {
            // Slot 0, 1, 2 = Boccette di pozioni (Potions)
            // Slot 3 = Ingrediente (Ingredient)
            // Slot 4 = Polvere di blaze (Fuel)
            // Slot 5-31 = Inventario (Main inventory)
            // Slot 32-40 = Hotbar

            // Check Blaze Powder (Fuel) in slot 4
            if (autoFuel.isOn() && brewingMenu.getSlot(4).getItem().isEmpty()) {
                int fuelSlot = findItemInInventory(brewingMenu, Items.BLAZE_POWDER);
                if (fuelSlot != -1) {
                    moveItem(brewingMenu, fuelSlot);
                    tickTimer = (int) Math.round(delay.getValue());
                    return;
                }
            }

            // Check if finished brewing / take out completed potions (if slot 0-2 are potions and slot 3 is empty)
            boolean potionsReady = isPotionReady(brewingMenu);
            if (potionsReady) {
                for (int i = 0; i < 3; i++) {
                    if (!brewingMenu.getSlot(i).getItem().isEmpty()) {
                        moveItem(brewingMenu, i);
                        tickTimer = (int) Math.round(delay.getValue());
                        return;
                    }
                }
                if (autoClose.isOn()) {
                    mc.player.closeContainer();
                }
            }
        }
    }

    private boolean isPotionReady(BrewingStandMenu menu) {
        // Se lo slot dell'ingrediente (3) è vuoto e c'è almeno una boccetta non vuota in 0, 1 o 2
        if (!menu.getSlot(3).getItem().isEmpty()) return false;
        return !menu.getSlot(0).getItem().isEmpty() || !menu.getSlot(1).getItem().isEmpty() || !menu.getSlot(2).getItem().isEmpty();
    }

    private int findItemInInventory(BrewingStandMenu menu, net.minecraft.world.item.Item item) {
        for (int i = 5; i < menu.slots.size(); i++) {
            ItemStack stack = menu.getSlot(i).getItem();
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    private void moveItem(BrewingStandMenu menu, int slotId) {
        mc.gameMode.handleInventoryMouseClick(
                menu.containerId,
                slotId,
                0,
                ClickType.QUICK_MOVE,
                mc.player
        );
    }
}
