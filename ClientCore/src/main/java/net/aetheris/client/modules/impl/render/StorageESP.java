package net.aetheris.client.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public class StorageESP extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 16.0, 4.0, 32.0, 2.0, "blocks");

    public StorageESP() {
        super("StorageESP", "Evidenzia ceste, shulker e bauli con box colorati attraverso i muri.", Category.RENDER);
        addSetting(range);
    }

    /** Chiamato dal WorldRendererMixin a fine renderLevel. */
    public void render(Camera camera, DeltaTracker deltaTracker) {
        if (mc.player == null || mc.level == null) return;

        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());

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
        bufferSource.endBatch(RenderType.lines());
    }

    private int getColor(BlockEntity be) {
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
            vc.addVertex(pose, a[0], a[1], a[2]).setColor(r, g, b, 255).setNormal(pose, 0f, 1f, 0f);
            vc.addVertex(pose, b2[0], b2[1], b2[2]).setColor(r, g, b, 255).setNormal(pose, 0f, 1f, 0f);
        }
    }
}
