package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SelfTrap extends Module {
    private final SliderSetting placeDelaySetting = new SliderSetting("placeDelay", "Place Delay", "Ritardo Piazzamento", 0, 0, 10, 1, "ticks");
    private final BooleanSetting autoDisable = new BooleanSetting("autoDisable", "Auto Disable", "Disattiva Automaticamente", true);

    private static final BlockPos[] TRAP_OFFSETS = {
        new BlockPos(0, 1, 0),
        new BlockPos(1, 1, 0),
        new BlockPos(-1, 1, 0),
        new BlockPos(0, 1, 1),
        new BlockPos(0, 1, -1),
        new BlockPos(0, 2, 0)
    };

    private int placeDelay = 0;

    public SelfTrap() {
        super("SelfTrap", "Piazza blocchi attorno e sopra il giocatore per proteggerlo in PvP.", Category.COMBAT);
        addSetting(placeDelaySetting);
        addSetting(autoDisable);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (placeDelay > 0) { placeDelay--; return; }

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        BlockPos playerPos = mc.player.blockPosition();
        int blocksPlaced = 0;
        int maxPerTick = placeDelaySetting.getIntValue() == 0 ? 4 : 1;
        int remaining = 0;

        for (BlockPos offset : TRAP_OFFSETS) {
            BlockPos target = playerPos.offset(offset);

            if (!mc.level.getBlockState(target).canBeReplaced()) continue;

            // Il blocco sopra la testa (0,1,0) interseca la bbox del player ma va piazzato comunque
            boolean isHeadBlock = offset.getX() == 0 && offset.getZ() == 0 && offset.getY() == 1;
            AABB targetBox = new AABB(target);
            if (!isHeadBlock && mc.player.getBoundingBox().intersects(targetBox)) continue;

            BlockPos neighbor = getValidNeighbor(target);
            Direction side = getClickSide(target, neighbor);

            if (neighbor == null || side == null) { remaining++; continue; }

            int prev = mc.player.getInventory().selected;
            mc.player.getInventory().selected = blockSlot;

            Vec3 hitVec = new Vec3(neighbor.getX() + 0.5 + side.getStepX() * 0.5,
                                   neighbor.getY() + 0.5 + side.getStepY() * 0.5,
                                   neighbor.getZ() + 0.5 + side.getStepZ() * 0.5);

            BlockHitResult hitResult = new BlockHitResult(hitVec, side, neighbor, false);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
            mc.player.swing(InteractionHand.MAIN_HAND);

            mc.player.getInventory().selected = prev;
            blocksPlaced++;

            if (blocksPlaced >= maxPerTick) {
                placeDelay = placeDelaySetting.getIntValue();
                return;
            }
        }

        if (autoDisable.isOn() && blocksPlaced == 0 && remaining == 0) {
            this.toggle();
        }
    }

    private BlockPos getValidNeighbor(BlockPos pos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbor = pos.relative(side);
            if (!mc.level.getBlockState(neighbor).canBeReplaced()) {
                return neighbor;
            }
        }
        BlockPos below = pos.below();
        if (!mc.level.getBlockState(below).canBeReplaced()) return below;
        return null;
    }

    private Direction getClickSide(BlockPos target, BlockPos neighbor) {
        if (neighbor == null) return Direction.UP;
        for (Direction side : Direction.values()) {
            if (neighbor.relative(side).equals(target)) {
                return side;
            }
        }
        return Direction.UP;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
