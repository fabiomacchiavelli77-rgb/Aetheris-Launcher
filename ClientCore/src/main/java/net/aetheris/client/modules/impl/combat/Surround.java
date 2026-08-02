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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class Surround extends Module {
    private final SliderSetting placeDelaySetting = new SliderSetting("placeDelay", "Place Delay", "Ritardo Piazzamento", 0, 0, 10, 1, "ticks");
    private final BooleanSetting centerPlayer = new BooleanSetting("centerPlayer", "Center Player", "Centra Giocatore", true);
    private final BooleanSetting autoDisable = new BooleanSetting("autoDisable", "Auto Disable", "Disattiva Automaticamente", true);
    private final BooleanSetting obsidianOnly = new BooleanSetting("obsidianOnly", "Obsidian Only", "Solo Ossidiana", false);

    private static final BlockPos[] SURROUND_OFFSETS = {
        new BlockPos(1, 0, 0),
        new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1),
        new BlockPos(0, 0, -1)
    };

    private int placeDelay = 0;
    private BlockPos initialPos = null;

    public Surround() {
        super("Surround", "Circonda i piedi del giocatore con blocchi di ossidiana per protezione.", Category.COMBAT);
        addSetting(placeDelaySetting);
        addSetting(centerPlayer);
        addSetting(autoDisable);
        addSetting(obsidianOnly);
    }
    
    @Override
    public void onEnable() {
        if (mc.player != null) {
            initialPos = mc.player.blockPosition();
            if (centerPlayer.isOn()) {
                double cx = Math.floor(mc.player.getX()) + 0.5;
                double cz = Math.floor(mc.player.getZ()) + 0.5;
                mc.player.setPos(cx, mc.player.getY(), cz);
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        
        BlockPos playerPos = mc.player.blockPosition();
        if (autoDisable.isOn() && initialPos != null) {
            if (!playerPos.equals(initialPos)) {
                this.toggle();
                return;
            }
        }
        
        if (placeDelay > 0) { placeDelay--; return; }

        int blockSlot = findObsidianOrBlock();
        if (blockSlot == -1) return;

        int blocksPlaced = 0;
        int maxPerTick = placeDelaySetting.getIntValue() == 0 ? 4 : 1;

        for (BlockPos offset : SURROUND_OFFSETS) {
            BlockPos target = playerPos.offset(offset);
            
            // Controlla se il blocco può essere rimpiazzato (aria o fluido)
            if (!mc.level.getBlockState(target).canBeReplaced()) continue;

            // Evita di piazzare blocchi dentro la Hitbox del giocatore
            AABB targetBox = new AABB(target);
            if (mc.player.getBoundingBox().intersects(targetBox)) continue;

            // Trova una faccia/blocco adiacente valido contro cui cliccare
            BlockPos neighbor = getValidNeighbor(target);
            Direction side = getClickSide(target, neighbor);

            if (neighbor != null && side != null) {
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

    private int findObsidianOrBlock() {
        int bestFallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                var block = blockItem.getBlock();
                if (block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.ANVIL || block == Blocks.ENDER_CHEST) {
                    return i;
                }
                if (bestFallback == -1 && !obsidianOnly.isOn()) {
                    bestFallback = i;
                }
            }
        }
        return obsidianOnly.isOn() ? -1 : bestFallback;
    }
}
