package net.aetheris.client.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashSet;
import java.util.Set;
import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class StorageESP extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 16.0, 4.0, 32.0, 2.0, "blocks");
    private final BooleanSetting hideEmpty = new BooleanSetting("hideEmpty", "Hide Empty", "Nascondi Vuote", false);

    private final Set<BlockPos> emptyChests = new HashSet<>();
    private BlockPos lastInteractedPos = null;

    public StorageESP() {
        super("StorageESP", "Evidenzia ceste, shulker e bauli con box colorati attraverso i muri.", Category.RENDER);
        addSetting(range);
        addSetting(hideEmpty);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (mc.hitResult instanceof BlockHitResult bhr && mc.options.keyUse.isDown()) {
            BlockPos pos = bhr.getBlockPos();
            if (mc.level.getBlockEntity(pos) != null) {
                lastInteractedPos = pos.immutable();
            }
        }

        if (mc.player.containerMenu instanceof ChestMenu chestMenu) {
            Container container = chestMenu.getContainer();
            boolean isEmpty = true;
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }

            BlockPos pos = null;
            if (container instanceof BlockEntity be) {
                pos = be.getBlockPos();
            } else if (lastInteractedPos != null && mc.level.getBlockEntity(lastInteractedPos) != null) {
                pos = lastInteractedPos;
            }

            if (pos != null) {
                markContainerState(pos, isEmpty);
            }
        }
    }

    private void markContainerState(BlockPos pos, boolean isEmpty) {
        if (isEmpty) {
            emptyChests.add(pos);
            markAdjacentChestIfAny(pos, true);
        } else {
            emptyChests.remove(pos);
            markAdjacentChestIfAny(pos, false);
        }
    }

    private void markAdjacentChestIfAny(BlockPos pos, boolean isEmpty) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adj = pos.relative(dir);
                BlockState adjState = mc.level.getBlockState(adj);
                if (adjState.getBlock() == state.getBlock()) {
                    if (isEmpty) emptyChests.add(adj);
                    else emptyChests.remove(adj);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        emptyChests.clear();
        lastInteractedPos = null;
    }

    /** Chiamato dal WorldRendererMixin a fine renderLevel. */
    public void render(Camera camera, DeltaTracker deltaTracker) {
        if (mc.player == null || mc.level == null) return;

        Vec3 camPos = camera.position();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderTypes.lines());

        int rangeBlocks = range.getIntValue();
        BlockPos center = mc.player.blockPosition();
        int minCX = (center.getX() - rangeBlocks) >> 4;
        int maxCX = (center.getX() + rangeBlocks) >> 4;
        int minCZ = (center.getZ() - rangeBlocks) >> 4;
        int maxCZ = (center.getZ() + rangeBlocks) >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    if (Math.abs(pos.getX() - center.getX()) > rangeBlocks) continue;
                    if (Math.abs(pos.getZ() - center.getZ()) > rangeBlocks) continue;

                    BlockEntity be = mc.level.getBlockEntity(pos);
                    if (be == null) continue;

                    int color = getColor(be);
                    if (color == 0) continue;

                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;

                    drawBox(vc, poseStack.last(), pos, r, g, b);
                }
            }
        }

        poseStack.popPose();
        bufferSource.endBatch(net.minecraft.client.renderer.rendertype.RenderTypes.lines());
    }

    private int getColor(BlockEntity be) {
        if (emptyChests.contains(be.getBlockPos())) {
            return hideEmpty.isOn() ? 0 : 0x707070;
        }

        if (be instanceof EnderChestBlockEntity) return 0x8B0000;
        if (be instanceof ShulkerBoxBlockEntity) return 0xA020F0;
        if (be instanceof BaseContainerBlockEntity) return 0xE8A33D;
        return 0;
    }

    private void drawBox(VertexConsumer vc, PoseStack.Pose pose, BlockPos pos, int r, int g, int b) {
        float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        // 8 vertici
        float[][] corners = {
            {x, y, z}, {x + 1, y, z}, {x + 1, y, z + 1}, {x, y, z + 1},
            {x, y + 1, z}, {x + 1, y + 1, z}, {x + 1, y + 1, z + 1}, {x, y + 1, z + 1}
        };
        // 12 spigoli
        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int[] edge : edges) {
            float[] a = corners[edge[0]];
            float[] b2 = corners[edge[1]];
            vc.addVertex(pose, a[0], a[1], a[2]).setColor(r, g, b, 255).setNormal(pose, 0f, 1f, 0f).setLineWidth(1.0f);
            vc.addVertex(pose, b2[0], b2[1], b2[2]).setColor(r, g, b, 255).setNormal(pose, 0f, 1f, 0f).setLineWidth(1.0f);
        }
    }
}
