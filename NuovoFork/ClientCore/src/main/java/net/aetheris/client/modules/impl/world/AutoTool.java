package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AutoTool extends Module {
    private int lastSlot = -1;

    public AutoTool() {
        super("AutoTool", "Seleziona automaticamente lo strumento migliore.", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.hitResult == null) return;
        if (!mc.options.keyAttack.isDown()) return;
        if (mc.gameMode == null || !mc.gameMode.isDestroying()) return;

        BlockPos pos = mc.hitResult.getBlockPos();
        if (pos == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (state.isAir()) return;

        int best = findBestTool(state);
        if (best != -1 && best != mc.player.getInventory().selected) {
            if (lastSlot == -1) lastSlot = mc.player.getInventory().selected;
            mc.player.getInventory().selected = best;
        }
    }

    private int findBestTool(BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) { bestSpeed = speed; bestSlot = i; }
        }
        return bestSlot;
    }

    @Override
    public void onDisable() {
        if (lastSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = lastSlot;
            lastSlot = -1;
        }
    }
}
