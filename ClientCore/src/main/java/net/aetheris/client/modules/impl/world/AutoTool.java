package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AutoTool extends Module {
    private final BooleanSetting switchBack = new BooleanSetting("switchBack", "Switch Back", "Ripristina Oggetto", true);
    private final BooleanSetting saveDurability = new BooleanSetting("saveDurability", "Save Durability", "Salva Durabilità", true);
    
    private int lastSlot = -1;

    public AutoTool() {
        super("AutoTool", "Seleziona automaticamente lo strumento migliore per il blocco che stai scavando.", Category.WORLD);
        addSetting(switchBack);
        addSetting(saveDurability);
    }

    public void updateTool(BlockPos pos) {
        if (mc.player == null || mc.level == null || pos == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        int bestSlot = findBestTool(state);
        if (bestSlot != -1 && bestSlot != mc.player.getInventory().selected) {
            if (lastSlot == -1) {
                lastSlot = mc.player.getInventory().selected;
            }
            mc.player.getInventory().selected = bestSlot;
        }
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (saveDurability.isOn() && stack.isDamageableItem() && (stack.getMaxDamage() - stack.getDamageValue() <= 2)) {
                continue;
            }

            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    @Override
    public void onDisable() {
        if (switchBack.isOn() && lastSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = lastSlot;
            lastSlot = -1;
        }
    }
}
