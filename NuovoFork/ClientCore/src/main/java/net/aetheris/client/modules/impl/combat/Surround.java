package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

public class Surround extends Module {
    private static final BlockPos[] OFFSETS = {
        new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
        new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1),
        new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1)
    };
    private int placeDelay = 0;

    public Surround() {
        super("Surround", "Circonda il player con blocchi per protezione.", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (placeDelay > 0) { placeDelay--; return; }

        BlockPos playerPos = mc.player.blockPosition();
        int blockSlot = findBlock();

        for (BlockPos offset : OFFSETS) {
            BlockPos target = playerPos.offset(offset);
            if (mc.level.getBlockState(target).canBeReplaced()) {
                if (blockSlot == -1) return;

                int prev = mc.player.getInventory().selected;
                mc.player.getInventory().selected = blockSlot;

                Vec3 hit = new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
                BlockHitResult result = new BlockHitResult(hit, Direction.UP, target.below(), false);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result);
                mc.player.swing(InteractionHand.MAIN_HAND);

                mc.player.getInventory().selected = prev;
                placeDelay = 3;
                return;
            }
        }
    }

    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
