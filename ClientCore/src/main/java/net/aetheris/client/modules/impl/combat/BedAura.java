package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BedAura extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 6.0, 1.0, 10.0, 1.0, "blocks");
    private final SliderSetting delay = new SliderSetting("delay", "Delay", "Ritardo", 2, 0, 10, 1, "ticks");
    private final BooleanSetting rotate = new BooleanSetting("rotate", "Rotate", "Ruota", true);

    private int cooldown = 0;

    public BedAura() {
        super("BedAura", "Esplode automaticamente i letti vicini (Nether/End).", Category.COMBAT);
        addSetting(range);
        addSetting(delay);
        addSetting(rotate);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.player.isSleeping()) return;

        if (cooldown > 0) { cooldown--; return; }

        BlockPos bed = findBed();
        if (bed == null) return;

        // Serve una mano vuota: con un item in mano il click piazzerebbe un blocco invece di esplodere il letto
        InteractionHand hand;
        if (mc.player.getMainHandItem().isEmpty()) hand = InteractionHand.MAIN_HAND;
        else if (mc.player.getOffhandItem().isEmpty()) hand = InteractionHand.OFF_HAND;
        else return;

        Vec3 center = Vec3.atCenterOf(bed);
        if (rotate.isOn()) {
            lookAt(center);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), false));
        }

        BlockHitResult hit = new BlockHitResult(center, Direction.UP, bed, false);
        mc.gameMode.useItemOn(mc.player, hand, hit);
        mc.player.swing(hand);

        cooldown = delay.getIntValue();
    }

    private BlockPos findBed() {
        BlockPos center = mc.player.blockPosition();
        int r = range.getIntValue();
        BlockPos[] best = { null };
        BlockPos.betweenClosedStream(center.offset(-r, -r, -r), center.offset(r, r, r))
            .forEach(pos -> {
                if (mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                    BlockPos found = pos.immutable();
                    if (best[0] == null || found.distSqr(center) < best[0].distSqr(center)) {
                        best[0] = found;
                    }
                }
            });
        return best[0];
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
