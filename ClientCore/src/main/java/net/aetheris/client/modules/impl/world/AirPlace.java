package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AirPlace extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Raggio", 4.5, 1.0, 6.0, 0.5, "blocks");
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo", 2.0, 0.0, 20.0, 1.0, "ticks");
    private final BooleanSetting autoPlace = new BooleanSetting("autoPlace", "Auto Place", "Piazzamento Automatico", false);

    private int placeCooldown = 0;

    public AirPlace() {
        super("AirPlace", "Piazza blocchi nell'aria senza bisogno di un blocco di supporto.", Category.WORLD);
        addSetting(range);
        addSetting(delay);
        addSetting(autoPlace);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (placeCooldown > 0) {
            placeCooldown--;
            return;
        }

        // Action triggered by holding right-click or if autoPlace is enabled
        if (!autoPlace.isOn() && !mc.options.keyUse.isDown()) return;

        // Check if player is holding a BlockItem
        InteractionHand hand = null;
        if (mc.player.getMainHandItem().getItem() instanceof BlockItem) {
            hand = InteractionHand.MAIN_HAND;
        } else if (mc.player.getOffhandItem().getItem() instanceof BlockItem) {
            hand = InteractionHand.OFF_HAND;
        }
        if (hand == null) return;

        // Trace along player's look vector for an air block
        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getLookAngle();
        double maxDist = range.getValue();

        BlockPos targetPos = null;
        for (double d = 1.0; d <= maxDist; d += 0.5) {
            Vec3 sampleVec = eyePos.add(lookVec.scale(d));
            BlockPos pos = BlockPos.containing(sampleVec);

            if (mc.level.getBlockState(pos).canBeReplaced()) {
                AABB posBox = new AABB(pos);
                if (!mc.player.getBoundingBox().intersects(posBox)) {
                    targetPos = pos;
                    break;
                }
            }
        }

        if (targetPos == null) return;

        Direction side = getNearestDirection(lookVec.reverse());
        Vec3 hitVec = Vec3.atCenterOf(targetPos);
        BlockHitResult airHitResult = new BlockHitResult(hitVec, side, targetPos, false);

        mc.gameMode.useItemOn(mc.player, hand, airHitResult);
        mc.player.swing(hand);
        placeCooldown = delay.getIntValue();
    }

    private Direction getNearestDirection(Vec3 vec) {
        Direction best = Direction.UP;
        double bestDot = -Double.MAX_VALUE;
        for (Direction d : Direction.values()) {
            double dot = vec.x * d.getStepX() + vec.y * d.getStepY() + vec.z * d.getStepZ();
            if (dot > bestDot) {
                bestDot = dot;
                best = d;
            }
        }
        return best;
    }
}
