package net.aetheris.client.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Trajectories extends Module {

    private final BooleanSetting bows = new BooleanSetting("bows", "Bows & Crossbows", "Archi e Balestre", true);
    private final BooleanSetting pearls = new BooleanSetting("pearls", "Ender Pearls", "Perle Ender", true);
    private final BooleanSetting snowballs = new BooleanSetting("snowballs", "Snowballs & Eggs", "Palle di Neve e Uova", true);
    private final BooleanSetting potions = new BooleanSetting("potions", "Potions & Bottles", "Pozioni e Boccette", true);
    private final BooleanSetting landingBox = new BooleanSetting("landingBox", "Landing Box", "Box d'Impatto", true);

    public Trajectories() {
        super("Trajectories", "Mostra la traiettoria di volo di archi, perle, palle di neve e altri proiettili.", Category.RENDER);
        addSetting(bows);
        addSetting(pearls);
        addSetting(snowballs);
        addSetting(potions);
        addSetting(landingBox);
    }

    public void render(Camera camera, DeltaTracker deltaTracker) {
        if (mc.player == null || mc.level == null) return;

        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty() || !isSupportedItem(stack)) {
            stack = mc.player.getOffhandItem();
        }
        if (stack.isEmpty() || !isSupportedItem(stack)) {
            return;
        }

        ProjectileInfo info = getProjectileInfo(stack);
        if (info == null) return;

        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot() + info.pitchOffset;

        double dirX = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double dirY = -Math.sin(Math.toRadians(pitch));
        double dirZ = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

        double motionX = dirX * info.velocity;
        double motionY = dirY * info.velocity;
        double motionZ = dirZ * info.velocity;

        Vec3 eyePos = mc.player.getEyePosition(deltaTracker.getGameTimeDeltaPartialTick(true));
        double posX = eyePos.x;
        double posY = eyePos.y;
        double posZ = eyePos.z;

        List<Vec3> path = new ArrayList<>();
        path.add(new Vec3(posX, posY, posZ));

        HitResult hitResult = null;

        for (int step = 0; step < 300; step++) {
            Vec3 start = new Vec3(posX, posY, posZ);
            Vec3 end = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
            BlockHitResult blockHit = mc.level.clip(context);

            if (blockHit.getType() != HitResult.Type.MISS) {
                end = blockHit.getLocation();
                hitResult = blockHit;
            }

            AABB stepBB = new AABB(start.x, start.y, start.z, end.x, end.y, end.z).inflate(1.0);
            List<Entity> entities = mc.level.getEntities(mc.player, stepBB, e -> e.isAlive() && !e.isSpectator() && e.isPickable());

            EntityHitResult entityHit = null;
            double closestDistance = start.distanceTo(end);

            for (Entity entity : entities) {
                AABB entityBB = entity.getBoundingBox().inflate(0.3);
                Optional<Vec3> intercept = entityBB.clip(start, end);
                if (intercept.isPresent()) {
                    double dist = start.distanceTo(intercept.get());
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        entityHit = new EntityHitResult(entity, intercept.get());
                    }
                }
            }

            if (entityHit != null) {
                hitResult = entityHit;
                end = entityHit.getLocation();
            }

            path.add(end);

            if (hitResult != null) {
                break;
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            boolean inWater = mc.level.getFluidState(BlockPos.containing(posX, posY, posZ)).is(FluidTags.WATER);
            double currentDrag = inWater ? 0.8 : info.drag;

            motionX *= currentDrag;
            motionY *= currentDrag;
            motionZ = (motionZ * currentDrag) - info.gravity;
        }

        renderTrajectory(camera, path, hitResult);
    }

    private void renderTrajectory(Camera camera, List<Vec3> path, HitResult hitResult) {
        Vec3 camPos = camera.position();
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vc = bufferSource.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.lines());

        int r = 50, g = 220, b = 255, a = 255;
        if (hitResult != null) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                r = 255; g = 50; b = 50;
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                r = 50; g = 255; b = 100;
            }
        }

        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < path.size() - 1; i++) {
            Vec3 p1 = path.get(i);
            Vec3 p2 = path.get(i + 1);

            vc.addVertex(pose, (float) p1.x, (float) p1.y, (float) p1.z)
              .setColor(r, g, b, a)
              .setNormal(pose, 0f, 1f, 0f);

            vc.addVertex(pose, (float) p2.x, (float) p2.y, (float) p2.z)
              .setColor(r, g, b, a)
              .setNormal(pose, 0f, 1f, 0f);
        }

        if (landingBox.isOn() && hitResult != null) {
            Vec3 landing = hitResult.getLocation();
            AABB box = new AABB(landing.x - 0.15, landing.y - 0.15, landing.z - 0.15,
                                landing.x + 0.15, landing.y + 0.15, landing.z + 0.15);
            drawBox(vc, pose, box, r, g, b, 255);
        }

        poseStack.popPose();
        bufferSource.endBatch(net.minecraft.client.renderer.rendertype.RenderTypes.lines());
    }

    private void drawBox(VertexConsumer vc, PoseStack.Pose pose, AABB box, int r, int g, int b, int a) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;

        float[][] corners = {
            {minX, minY, minZ}, {maxX, minY, minZ}, {maxX, minY, maxZ}, {minX, minY, maxZ},
            {minX, maxY, minZ}, {maxX, maxY, minZ}, {maxX, maxY, maxZ}, {minX, maxY, maxZ}
        };
        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (int[] edge : edges) {
            float[] p1 = corners[edge[0]];
            float[] p2 = corners[edge[1]];
            vc.addVertex(pose, p1[0], p1[1], p1[2]).setColor(r, g, b, a).setNormal(pose, 0f, 1f, 0f);
            vc.addVertex(pose, p2[0], p2[1], p2[2]).setColor(r, g, b, a).setNormal(pose, 0f, 1f, 0f);
        }
    }

    private boolean isSupportedItem(ItemStack stack) {
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem || stack.getItem() instanceof TridentItem) {
            return bows.isOn();
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return pearls.isOn();
        }
        if (stack.is(Items.SNOWBALL) || stack.is(Items.EGG)) {
            return snowballs.isOn();
        }
        if (stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION) || stack.is(Items.EXPERIENCE_BOTTLE)) {
            return potions.isOn();
        }
        if (stack.is(Items.WIND_CHARGE)) {
            return snowballs.isOn();
        }
        return false;
    }

    private ProjectileInfo getProjectileInfo(ItemStack stack) {
        if (stack.getItem() instanceof BowItem) {
            float drawProgress = 1.0f;
            if (mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 0) {
                int useTicks = stack.getUseDuration(mc.player) - mc.player.getUseItemRemainingTicks();
                drawProgress = BowItem.getPowerForTime(useTicks);
            }
            if (drawProgress < 0.1f) return null;
            return new ProjectileInfo(drawProgress * 3.0f, 0.05f, 0.99f, 0.0f);
        }
        if (stack.getItem() instanceof CrossbowItem) {
            if (!CrossbowItem.isCharged(stack)) return null;
            return new ProjectileInfo(3.15f, 0.05f, 0.99f, 0.0f);
        }
        if (stack.getItem() instanceof TridentItem) {
            float drawProgress = 1.0f;
            if (mc.player.isUsingItem() && mc.player.getUseItemRemainingTicks() > 0) {
                int useTicks = stack.getUseDuration(mc.player) - mc.player.getUseItemRemainingTicks();
                drawProgress = Math.min((float) useTicks / 10.0f, 1.0f);
            }
            if (drawProgress < 0.1f) return null;
            return new ProjectileInfo(drawProgress * 2.5f, 0.05f, 0.99f, 0.0f);
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return new ProjectileInfo(1.5f, 0.03f, 0.99f, 0.0f);
        }
        if (stack.is(Items.SNOWBALL) || stack.is(Items.EGG) || stack.is(Items.WIND_CHARGE)) {
            return new ProjectileInfo(1.5f, 0.03f, 0.99f, 0.0f);
        }
        if (stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
            return new ProjectileInfo(0.5f, 0.05f, 0.99f, -20.0f);
        }
        if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            return new ProjectileInfo(0.7f, 0.07f, 0.99f, -20.0f);
        }
        return null;
    }

    private static class ProjectileInfo {
        final float velocity;
        final float gravity;
        final float drag;
        final float pitchOffset;

        ProjectileInfo(float velocity, float gravity, float drag, float pitchOffset) {
            this.velocity = velocity;
            this.gravity = gravity;
            this.drag = drag;
            this.pitchOffset = pitchOffset;
        }
    }
}
