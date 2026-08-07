package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AutoSmelter extends Module {
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo", 3.0, 0.0, 20.0, 1.0, "ticks");
    private final BooleanSetting autoFuel = new BooleanSetting("autoFuel", "Auto Fuel", "Inserisci carburante", true);
    private final BooleanSetting autoInput = new BooleanSetting("autoInput", "Auto Input", "Inserisci materiale da cuocere", true);
    private final BooleanSetting autoTake = new BooleanSetting("autoTake", "Auto Take", "Raccogli prodotti cotti", true);

    private int tickTimer = 0;

    public AutoSmelter() {
        super("AutoSmelter", "Gestore automatico delle fornaci.", Category.WORLD);
        addSetting(delay);
        addSetting(autoFuel);
        addSetting(autoInput);
        addSetting(autoTake);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (tickTimer > 0) {
            tickTimer--;
            return;
        }

        if (mc.player.containerMenu instanceof AbstractFurnaceMenu furnace) {
            // Slot 0 = Input (Materiale da cuocere)
            // Slot 1 = Fuel (Carburante)
            // Slot 2 = Output (Risultato)
            // Slot 3-38 = Inventario e Hotbar

            // 1. Take output if present
            if (autoTake.isOn() && !furnace.getSlot(2).getItem().isEmpty()) {
                moveItem(furnace, 2);
                tickTimer = (int) Math.round(delay.getValue());
                return;
            }

            // 2. Refill Fuel if slot 1 empty
            if (autoFuel.isOn() && furnace.getSlot(1).getItem().isEmpty()) {
                int fuelSlot = findFuelSlot(furnace);
                if (fuelSlot != -1) {
                    moveItem(furnace, fuelSlot);
                    tickTimer = (int) Math.round(delay.getValue());
                    return;
                }
            }

            // 3. Refill Input if slot 0 empty
            if (autoInput.isOn() && furnace.getSlot(0).getItem().isEmpty()) {
                int inputSlot = findInputSlot(furnace);
                if (inputSlot != -1) {
                    moveItem(furnace, inputSlot);
                    tickTimer = (int) Math.round(delay.getValue());
                    return;
                }
            }
        }
    }

    private int findFuelSlot(AbstractFurnaceMenu furnace) {
        for (int i = 3; i < furnace.slots.size(); i++) {
            Slot slot = furnace.getSlot(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && isFuel(furnace, stack)) {
                return i;
            }
        }
        return -1;
    }

    private int findInputSlot(AbstractFurnaceMenu furnace) {
        for (int i = 3; i < furnace.slots.size(); i++) {
            Slot slot = furnace.getSlot(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && !isFuel(furnace, stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isFuel(AbstractFurnaceMenu furnace, ItemStack stack) {
        return ((net.aetheris.client.mixins.AbstractFurnaceMenuAccessor) furnace).invokeIsFuel(stack);
    }

    private void moveItem(AbstractFurnaceMenu furnace, int slotId) {
        mc.gameMode.handleInventoryMouseClick(
                furnace.containerId,
                slotId,
                0,
                ClickType.QUICK_MOVE,
                mc.player
        );
    }
}
