package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Reach extends Module {
    
    private final SliderSetting combatReach = new SliderSetting("combatReach", "Combat Reach", "Portata Combattimento", 6.0, 3.0, 15.0, 0.5, "blocks");
    private final SliderSetting blockReach = new SliderSetting("blockReach", "Block Reach", "Portata Blocchi", 5.5, 4.5, 6.0, 0.1, "blocks");
    private final BooleanSetting tpReach = new BooleanSetting("tpReach", "TP-Reach (Bypass >6m)", "TP-Reach (Supera 6m)", true);

    public Reach() {
        super("Reach", "Estende la portata di attacco. Con TP-Reach attivo permette di colpire fino a 15 blocchi.", Category.COMBAT);
        addSetting(combatReach);
        addSetting(blockReach);
        addSetting(tpReach);
    }

    public float getReachDistance() { return combatReach.getValue().floatValue(); }
    public boolean isTpReachEnabled() { return tpReach.isOn(); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        var entityRange = mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var blockRange = mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (entityRange != null) entityRange.setBaseValue(combatReach.getValue());
        if (blockRange != null) blockRange.setBaseValue(blockReach.getValue());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        var entityRange = mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var blockRange = mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (entityRange != null) entityRange.setBaseValue(3.0);
        if (blockRange != null) blockRange.setBaseValue(4.5);
    }

    public Entity getTargetInLookVector(float maxDist) {
        if (mc.player == null || mc.level == null) return null;
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getViewVector(1.0f);
        Vec3 maxPos = eyePos.add(lookVec.x * maxDist, lookVec.y * maxDist, lookVec.z * maxDist);
        AABB searchBox = mc.player.getBoundingBox().expandTowards(lookVec.scale(maxDist)).inflate(1.0, 1.0, 1.0);

        Entity closest = null;
        double minDst = Double.MAX_VALUE;

        for (Entity entity : mc.level.getEntities(mc.player, searchBox, e -> e.isAlive() && e instanceof net.minecraft.world.entity.LivingEntity)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            java.util.Optional<Vec3> hit = aabb.clip(eyePos, maxPos);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < minDst && dist <= maxDist) {
                    minDst = dist;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    public boolean tryTpAttack(Entity target) {
        if (!isEnabled() || !tpReach.isOn() || mc.player == null || mc.getConnection() == null) return false;
        double dist = mc.player.distanceTo(target);
        if (dist > 2.8 && dist <= combatReach.getValue()) {
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();

            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();

            int steps = (int) Math.ceil(dist / 3.0);
            for (int i = 1; i <= steps; i++) {
                double frac = (double) i / steps;
                double ix = px + (tx - px) * frac;
                double iy = py + (ty - py) * frac;
                double iz = pz + (tz - pz) * frac;
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(ix, iy, iz, mc.player.onGround(), false));
            }
            return true;
        }
        return false;
    }

    public void finishTpAttack(Entity target) {
        if (!isEnabled() || !tpReach.isOn() || mc.player == null || mc.getConnection() == null) return;
        double dist = mc.player.distanceTo(target);
        if (dist > 2.8 && dist <= combatReach.getValue()) {
            double px = mc.player.getX();
            double py = mc.player.getY();
            double pz = mc.player.getZ();

            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();

            int steps = (int) Math.ceil(dist / 3.0);
            for (int i = steps - 1; i >= 0; i--) {
                double frac = (double) i / steps;
                double ix = px + (tx - px) * frac;
                double iy = py + (ty - py) * frac;
                double iz = pz + (tz - pz) * frac;
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(ix, iy, iz, mc.player.onGround(), false));
            }
        }
    }
}
