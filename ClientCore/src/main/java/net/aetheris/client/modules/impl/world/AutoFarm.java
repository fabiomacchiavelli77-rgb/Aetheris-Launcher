package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoFarm extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Raggio di azione", 4.5, 1.0, 6.0, 0.5);
    private final BooleanSetting replant = new BooleanSetting("replant", "Replant", "Risemina automaticamente", true);

    public AutoFarm() {
        super("AutoFarm", Category.WORLD);
        addSetting(range);
        addSetting(replant);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.getConnection() == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int r = (int) Math.ceil(range.getValue());

        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (mc.player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > range.getValue() * range.getValue()) {
                        continue;
                    }

                    BlockState state = mc.level.getBlockState(pos);
                    Block block = state.getBlock();

                    boolean isMature = false;

                    if (block instanceof CropBlock crop) {
                        isMature = crop.isMaxAge(state);
                    } else if (block instanceof NetherWartBlock wart) {
                        isMature = state.getValue(NetherWartBlock.AGE) >= 3;
                    }

                    if (isMature) {
                        // Rompe la coltura matura
                        mc.getConnection().send(new ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                                pos, Direction.UP
                        ));
                        mc.getConnection().send(new ServerboundPlayerActionPacket(
                                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                                pos, Direction.UP
                        ));
                        mc.level.destroyBlock(pos, true);

                        // Risemina se abilitato
                        if (replant.isOn()) {
                            tryReplant(pos);
                        }
                        return; // Processa un blocco per tick per evitare Kick Spam
                    }
                }
            }
        }
    }

    private void tryReplant(BlockPos pos) {
        int seedSlot = findSeedSlot();
        if (seedSlot != -1) {
            int oldSlot = mc.player.getInventory().selected;
            mc.player.getInventory().selected = seedSlot;

            BlockHitResult hitResult = new BlockHitResult(
                    new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5),
                    Direction.UP, pos.below(), false
            );

            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);

            mc.player.getInventory().selected = oldSlot;
        }
    }

    private int findSeedSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                Block b = blockItem.getBlock();
                if (b instanceof CropBlock || b instanceof NetherWartBlock) {
                    return i;
                }
            }
        }
        return -1;
    }
}
