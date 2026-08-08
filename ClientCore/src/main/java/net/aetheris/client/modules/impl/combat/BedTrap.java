package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BedTrap extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 6.0, 1.0, 10.0, 0.5, "blocks");
    private final SliderSetting placeDelay = new SliderSetting("placeDelay", "Place Delay", "Ritardo Piazzamento", 2.0, 0.0, 10.0, 1.0, "ticks");
    private final SliderSetting explodeDelay = new SliderSetting("explodeDelay", "Explode Delay", "Ritardo Esplosione", 1.0, 0.0, 10.0, 1.0, "ticks");
    private final BooleanSetting autoObsidian = new BooleanSetting("autoObsidian", "Auto Obsidian", "Ossidiana Auto", true);
    private final BooleanSetting dimensionCheck = new BooleanSetting("dimensionCheck", "Dimension Check", "Controllo Dimensione", true);
    private final BooleanSetting rotate = new BooleanSetting("rotate", "Rotate", "Ruota", true);

    private int placeCooldown = 0;
    private int explodeCooldown = 0;

    public BedTrap() {
        super("BedTrap", "Piazza ed esplode automaticamente i letti nei pressi dei nemici (Nether/End).", Category.COMBAT);
        addSetting(range);
        addSetting(placeDelay);
        addSetting(explodeDelay);
        addSetting(autoObsidian);
        addSetting(dimensionCheck);
        addSetting(rotate);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (dimensionCheck.isOn() && mc.level.dimension() == net.minecraft.world.level.Level.OVERWORLD) return;
        if (mc.player.isSleeping()) return;

        if (placeCooldown > 0) placeCooldown--;
        if (explodeCooldown > 0) explodeCooldown--;

        // 1. Detonate existing nearby bed
        BlockPos existingBed = findBedNearTarget();
        if (existingBed != null && explodeCooldown <= 0) {
            detonateBed(existingBed);
            explodeCooldown = explodeDelay.getIntValue();
            return;
        }

        // 2. Place bed near target
        if (placeCooldown <= 0) {
            Player target = findTarget();
            if (target != null) {
                int bedSlot = findBedSlot();
                if (bedSlot != -1) {
                    tryPlaceBed(target, bedSlot);
                }
            }
        }
    }

    private void detonateBed(BlockPos bedPos) {
        InteractionHand hand = mc.player.getMainHandItem().isEmpty() ? InteractionHand.MAIN_HAND :
                               (mc.player.getOffhandItem().isEmpty() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);

        Vec3 center = Vec3.atCenterOf(bedPos);
        if (rotate.isOn()) {
            lookAt(center);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
        }

        BlockHitResult hit = new BlockHitResult(center, Direction.UP, bedPos, false);
        mc.gameMode.useItemOn(mc.player, hand, hit);
        mc.player.swing(hand);
    }

    private void tryPlaceBed(Player target, int bedSlot) {
        BlockPos targetPos = target.blockPosition();
        Direction[] cardinals = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

        for (Direction dir : cardinals) {
            BlockPos footPos = targetPos.relative(dir);
            BlockPos headPos = footPos.relative(dir);

            if (mc.player.distanceToSqr(Vec3.atCenterOf(footPos)) > range.getValue() * range.getValue()) continue;
            if (!mc.level.getBlockState(footPos).canBeReplaced()) continue;
            if (!mc.level.getBlockState(headPos).canBeReplaced()) continue;

            BlockPos underFoot = footPos.below();
            BlockPos underHead = headPos.below();

            boolean supportFoot = !mc.level.getBlockState(underFoot).canBeReplaced();
            boolean supportHead = !mc.level.getBlockState(underHead).canBeReplaced();

            if ((!supportFoot || !supportHead) && autoObsidian.isOn()) {
                int obsidianSlot = findObsidianSlot();
                if (obsidianSlot != -1) {
                    if (!supportFoot) placeBlock(underFoot, obsidianSlot);
                    if (!supportHead) placeBlock(underHead, obsidianSlot);
                    supportFoot = !mc.level.getBlockState(underFoot).canBeReplaced();
                    supportHead = !mc.level.getBlockState(underHead).canBeReplaced();
                }
            }

            if (!supportFoot || !supportHead) continue;

            float yaw = dir.toYRot();
            if (rotate.isOn()) {
                mc.player.setYRot(yaw);
                mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                    yaw, mc.player.getXRot(), mc.player.onGround(), false));
            }

            int prevSlot = mc.player.getInventory().selected;
            mc.player.getInventory().selected = bedSlot;

            Vec3 hitVec = Vec3.atCenterOf(underFoot).add(0, 0.5, 0);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, underFoot, false);
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
            mc.player.swing(InteractionHand.MAIN_HAND);

            mc.player.getInventory().selected = prevSlot;
            placeCooldown = placeDelay.getIntValue();
            explodeCooldown = explodeDelay.getIntValue();
            return;
        }
    }

    private void placeBlock(BlockPos pos, int slot) {
        BlockPos neighbor = getValidNeighbor(pos);
        Direction side = getClickSide(pos, neighbor);
        if (neighbor == null || side == null) return;

        int prevSlot = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;

        Vec3 hitVec = Vec3.atCenterOf(neighbor).add(
            side.getStepX() * 0.5, side.getStepY() * 0.5, side.getStepZ() * 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, side, neighbor, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
        mc.player.swing(InteractionHand.MAIN_HAND);

        mc.player.getInventory().selected = prevSlot;
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

    private BlockPos findBedNearTarget() {
        Player target = findTarget();
        if (target == null) return null;

        BlockPos center = target.blockPosition();
        int r = (int) Math.ceil(range.getValue());
        BlockPos[] best = { null };

        BlockPos.betweenClosedStream(center.offset(-r, -r, -r), center.offset(r, r, r))
            .forEach(pos -> {
                if (mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                    BlockPos found = pos.immutable();
                    if (mc.player.distanceToSqr(Vec3.atCenterOf(found)) <= range.getValue() * range.getValue()) {
                        if (best[0] == null || target.distanceToSqr(Vec3.atCenterOf(found)) < target.distanceToSqr(Vec3.atCenterOf(best[0]))) {
                            best[0] = found;
                        }
                    }
                }
            });
        return best[0];
    }

    private Player findTarget() {
        Player best = null;
        double bestDist = range.getValue();
        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (!player.isAlive() || player.isSpectator() || player.isCreative()) continue;
            double dist = mc.player.distanceTo(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    private int findBedSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BedItem) {
                return i;
            }
        }
        return -1;
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == Blocks.OBSIDIAN || blockItem.getBlock() == Blocks.CRYING_OBSIDIAN) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void lookAt(Vec3 target) {
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.001) return;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYRot(yaw);
        mc.player.setXRot(pitch);
    }
}
