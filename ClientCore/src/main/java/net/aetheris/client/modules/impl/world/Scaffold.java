package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Scaffold extends Module {
    private int placeCooldown = 0;

    public Scaffold() {
        super("Scaffold", "Piazza blocchi sotto i piedi mentre cammini.", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (placeCooldown > 0) { placeCooldown--; return; }
        if (mc.player.input.forwardImpulse == 0 && mc.player.input.leftImpulse == 0) return;

        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).isAir() && !mc.level.getBlockState(below).canBeReplaced()) return;

        int slot = findBlock();
        if (slot == -1) return;

        int prev = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;

        Vec3 hit = new Vec3(below.getX() + 0.5, below.getY() + 0.5, below.getZ() + 0.5);
        BlockHitResult result = new BlockHitResult(hit, Direction.UP, below, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result);
        mc.player.swing(InteractionHand.MAIN_HAND);

        mc.player.getInventory().selected = prev;
        placeCooldown = 2;
    }

    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
