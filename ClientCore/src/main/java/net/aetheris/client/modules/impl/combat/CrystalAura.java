package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrystalAura extends Module {
    private final SliderSetting range = new SliderSetting("range", "Attack Range", "Portata Attacco", 5.0, 1.0, 8.0, 0.5, "blocks");
    private final SliderSetting cps = new SliderSetting("cps", "Attack Speed", "Velocità Attacco", 8.0, 1.0, 20.0, 0.5, "CPS");
    private final BooleanSetting cooldownSync = new BooleanSetting("cooldownSync", "Cooldown Sync", "Sinc. Cooldown", true);
    private final BooleanSetting rotate = new BooleanSetting("rotate", "Rotate", "Ruota", true);

    private final BooleanSetting autoPlace = new BooleanSetting("autoPlace", "Auto Place", "Piazzamento Auto", true);
    private final BooleanSetting autoObsidian = new BooleanSetting("autoObsidian", "Auto Obsidian", "Ossidiana Auto", true);
    private final SliderSetting placeRange = new SliderSetting("placeRange", "Place Range", "Portata Piazzamento", 5.0, 1.0, 8.0, 0.5, "blocks");
    private final SliderSetting targetRange = new SliderSetting("targetRange", "Target Range", "Portata Bersaglio", 10.0, 3.0, 16.0, 1.0, "blocks");
    private final SliderSetting placeDelay = new SliderSetting("placeDelay", "Place Delay", "Ritardo Piazzamento", 1.0, 0.0, 10.0, 1.0, "ticks");

    private int attackCooldown = 0;
    private int placeCooldown = 0;

    public CrystalAura() {
        super("CrystalAura", "Attacca e piazza automaticamente gli End Crystal vicini per esploderli.", Category.COMBAT);
        addSetting(range);
        addSetting(cps);
        addSetting(cooldownSync);
        addSetting(rotate);

        addSetting(autoPlace);
        addSetting(autoObsidian);
        addSetting(placeRange);
        addSetting(targetRange);
        addSetting(placeDelay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (placeCooldown > 0) placeCooldown--;

        // 1. Attack Phase
        boolean canAttack = true;
        if (cooldownSync.isOn()) {
            if (mc.player.getAttackStrengthScale(0.5f) < 0.9f) canAttack = false;
        } else {
            if (attackCooldown > 0) { attackCooldown--; canAttack = false; }
        }

        if (canAttack) {
            EndCrystal crystal = findCrystal();
            if (crystal != null) {
                if (rotate.isOn()) {
                    lookAt(crystal.position());
                    mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                        mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
                }

                mc.gameMode.attack(mc.player, crystal);
                mc.player.swing(InteractionHand.MAIN_HAND);

                if (!cooldownSync.isOn()) {
                    attackCooldown = (int) (20.0 / cps.getValue());
                }
            }
        }

        // 2. Placement Phase
        if (autoPlace.isOn() && placeCooldown <= 0) {
            Player target = findTarget();
            if (target != null) {
                int crystalSlot = findCrystalSlot();
                if (crystalSlot != -1) {
                    BlockPos placePos = findCrystalPlacePos(target);
                    if (placePos != null) {
                        placeCrystal(placePos, crystalSlot);
                        return;
                    }
                }

                if (autoObsidian.isOn()) {
                    int obsidianSlot = findObsidianSlot();
                    if (obsidianSlot != -1 && crystalSlot != -1) {
                        BlockPos obsidianPos = findObsidianPlacePos(target);
                        if (obsidianPos != null) {
                            placeObsidian(obsidianPos, obsidianSlot);
                        }
                    }
                }
            }
        }
    }

    private void placeCrystal(BlockPos pos, int slot) {
        Vec3 center = Vec3.atCenterOf(pos).add(0, 0.5, 0);
        if (rotate.isOn()) {
            lookAt(center);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
        }

        int prevSlot = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;

        BlockHitResult hitResult = new BlockHitResult(center, Direction.UP, pos, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
        mc.player.swing(InteractionHand.MAIN_HAND);

        mc.player.getInventory().selected = prevSlot;
        placeCooldown = placeDelay.getIntValue();
    }

    private void placeObsidian(BlockPos pos, int slot) {
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
        placeCooldown = placeDelay.getIntValue();
    }

    private BlockPos findCrystalPlacePos(Player target) {
        BlockPos targetPos = target.blockPosition();
        int r = (int) Math.ceil(placeRange.getValue());
        BlockPos best = null;
        double bestDistSqr = Double.MAX_VALUE;

        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = targetPos.offset(x, y, z);
                    if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > placeRange.getValue() * placeRange.getValue()) continue;
                    if (!canPlaceCrystalOn(pos)) continue;

                    double targetDistSqr = target.distanceToSqr(Vec3.atCenterOf(pos));
                    if (targetDistSqr <= 16.0 && targetDistSqr < bestDistSqr) {
                        bestDistSqr = targetDistSqr;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    private boolean canPlaceCrystalOn(BlockPos pos) {
        var state = mc.level.getBlockState(pos);
        if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) return false;

        BlockPos above = pos.above();
        if (!mc.level.getBlockState(above).isAir()) return false;

        AABB box = new AABB(above);
        for (Entity entity : mc.level.getEntities(null, box)) {
            if (entity.isAlive() && !(entity instanceof EndCrystal)) return false;
        }
        return true;
    }

    private BlockPos findObsidianPlacePos(Player target) {
        BlockPos targetPos = target.blockPosition();
        BlockPos[] candidates = {
            targetPos.east(), targetPos.west(), targetPos.north(), targetPos.south(),
            targetPos.below()
        };

        for (BlockPos pos : candidates) {
            if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > placeRange.getValue() * placeRange.getValue()) continue;
            if (!mc.level.getBlockState(pos).canBeReplaced()) continue;
            if (!mc.level.getBlockState(pos.above()).isAir()) continue;

            AABB box = new AABB(pos);
            if (mc.player.getBoundingBox().intersects(box) || target.getBoundingBox().intersects(box)) continue;

            BlockPos neighbor = getValidNeighbor(pos);
            if (neighbor != null) return pos;
        }
        return null;
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

    private Player findTarget() {
        Player best = null;
        double bestDist = targetRange.getValue();
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

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(Items.END_CRYSTAL)) {
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

    private EndCrystal findCrystal() {
        EndCrystal best = null;
        double bestDist = range.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) continue;
            if (!crystal.isAlive()) continue;
            double dist = mc.player.distanceTo(crystal);
            if (dist < bestDist) {
                bestDist = dist;
                best = crystal;
            }
        }
        return best;
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
