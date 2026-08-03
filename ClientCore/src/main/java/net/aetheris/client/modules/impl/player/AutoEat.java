package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class AutoEat extends Module {
    private final SliderSetting threshold = new SliderSetting("threshold", "Hunger Threshold", "Soglia Fame", 14.0, 1.0, 19.0, 1.0, "hunger");
    private final BooleanSetting eatInWater = new BooleanSetting("eatInWater", "Eat In Water", "Mangia In Acqua", false);

    public AutoEat() {
        super("AutoEat", "Consuma automaticamente il cibo quando la fame scende sotto la soglia.", Category.PLAYER);
        addSetting(threshold);
        addSetting(eatInWater);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        if (mc.player.isUsingItem()) return;
        if (mc.player.getFoodData().getFoodLevel() >= threshold.getIntValue()) return;
        if (!eatInWater.isOn() && mc.player.isInWater()) return;

        int slot = findFoodSlot();
        if (slot == -1) return;

        int prev = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.getInventory().selected = prev;
    }

    private int findFoodSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.get(DataComponents.FOOD) != null) {
                return i;
            }
        }
        return -1;
    }
}
