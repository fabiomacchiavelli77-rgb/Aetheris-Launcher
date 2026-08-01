package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Direction;

public class Surround extends Module {
    private final SliderSetting placeDelaySetting = new SliderSetting("placeDelay", "Place Delay", "Ritardo Piazzamento", 1, 0, 10, 1, "ticks");
    private final BooleanSetting autoDisable = new BooleanSetting("autoDisable", "Auto Disable", "Disattiva Automaticamente", true);

    private static final BlockPos[] OFFSETS = {
        new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
        new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1),
        new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1)
    };
    private int placeDelay = 0;
    private BlockPos initialPos = null;

    public Surround() {
        super("Surround", "Circonda il player con blocchi per protezione.", Category.COMBAT);
        addSetting(placeDelaySetting);
        addSetting(autoDisable);
    }
    
    @Override
    public void onEnable() {
        if (mc.player != null) {
            initialPos = mc.player.blockPosition();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        if (autoDisable.isOn() && initialPos != null) {
            if (!mc.player.blockPosition().equals(initialPos)) {
                this.toggle();
                return;
            }
        }
        
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
                placeDelay = placeDelaySetting.getIntValue();
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
