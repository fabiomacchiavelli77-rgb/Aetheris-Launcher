package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BowAimbot extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 40.0, 5.0, 100.0, 1.0, "blocks");
    private final SliderSetting fov = new SliderSetting("fov", "FOV Angle", "Angolo FOV", 90.0, 10.0, 360.0, 5.0, "°");
    private final SliderSetting speed = new SliderSetting("speed", "Rotation Speed", "Velocità Rotazione", 1.0, 0.1, 2.0, 0.05, "");
    private final BooleanSetting targetPlayers = new BooleanSetting("targetPlayers", "Target Players", "Bersaglia Giocatori", true);
    private final BooleanSetting targetMobs = new BooleanSetting("targetMobs", "Target Mobs", "Bersaglia Mob", true);
    private final BooleanSetting targetAnimals = new BooleanSetting("targetAnimals", "Target Animals", "Bersaglia Animali", false);
    private final BooleanSetting autoRelease = new BooleanSetting("autoRelease", "Auto Release", "Rilascio Automatico", false);
    private final BooleanSetting silentAim = new BooleanSetting("silentAim", "Predict Trajectory", "Predici Traiettoria", true);

    public BowAimbot() {
        super("BowAimbot", "Calcola e mira automaticamente la traiettoria dell'arco o della balestra verso i bersagli.", Category.COMBAT);
        addSetting(range);
        addSetting(fov);
        addSetting(speed);
        addSetting(targetPlayers);
        addSetting(targetMobs);
        addSetting(targetAnimals);
        addSetting(autoRelease);
        addSetting(silentAim);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        ItemStack item = mc.player.getMainHandItem();
        boolean isBow = item.getItem() instanceof BowItem;
        boolean isCrossbow = item.getItem() instanceof CrossbowItem;

        if (!isBow && !isCrossbow) {
            item = mc.player.getOffhandItem();
            isBow = item.getItem() instanceof BowItem;
            isCrossbow = item.getItem() instanceof CrossbowItem;
        }

        if (!isBow && !isCrossbow) return;

        float velocity = 0.0f;
        float gravity = 0.05f;

        if (isBow) {
            if (!mc.player.isUsingItem()) return;
            int useTicks = item.getUseDuration(mc.player) - mc.player.getUseItemRemainingTicks();
            float drawPower = BowItem.getPowerForTime(useTicks);
            if (drawPower < 0.1f) return;
            velocity = drawPower * 3.0f;
        } else {
            if (!CrossbowItem.isCharged(item) && !mc.player.isUsingItem()) return;
            velocity = 3.15f;
        }

        Entity target = findTarget();
        if (target == null) return;

        // Calculate leading position considering target velocity and arrow travel time
        Vec3 predictedPos = predictTargetPosition(target, velocity, gravity);
        if (predictedPos == null) return;

        // Calculate required yaw and pitch for ballistic curve trajectory
        Vec3 eyePos = mc.player.getEyePosition();
        double dx = predictedPos.x - eyePos.x;
        double dy = predictedPos.y - eyePos.y;
        double dz = predictedPos.z - eyePos.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);

        // Angle calculation taking gravity into account: pitch = atan((v^2 +- sqrt(v^4 - g*(g*x^2 + 2*y*v^2))) / (g*x))
        float pitch = calculateBallisticPitch((float) distXZ, (float) dy, velocity, gravity);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;

        float yawDiff = Mth.wrapDegrees(yaw - mc.player.getYRot());
        float pitchDiff = Mth.wrapDegrees(pitch - mc.player.getXRot());

        float rotStep = speed.getValue().floatValue() * 10.0f;
        float newYaw = mc.player.getYRot() + Math.copySign(Math.min(Math.abs(yawDiff), rotStep), yawDiff);
        float newPitch = mc.player.getXRot() + Math.copySign(Math.min(Math.abs(pitchDiff), rotStep), pitchDiff);

        mc.player.setYRot(newYaw);
        mc.player.setXRot(newPitch);

        if (isBow && autoRelease.isOn()) {
            int useTicks = item.getUseDuration(mc.player) - mc.player.getUseItemRemainingTicks();
            float drawPower = BowItem.getPowerForTime(useTicks);
            if (drawPower >= 1.0f && Math.abs(yawDiff) < 3.0f && Math.abs(pitchDiff) < 3.0f) {
                mc.gameMode.releaseUsingItem(mc.player);
            }
        }
    }

    private Vec3 predictTargetPosition(Entity target, float arrowVelocity, float gravity) {
        Vec3 targetEyePos = target.getEyePosition();
        Vec3 targetVel = target.getDeltaMovement();

        Vec3 playerEyePos = mc.player.getEyePosition();
        double distance = playerEyePos.distanceTo(targetEyePos);

        // Approximate flight time
        double timeOfFlight = distance / arrowVelocity;

        // Lead position prediction
        double predX = targetEyePos.x + targetVel.x * timeOfFlight;
        double predY = targetEyePos.y + (targetVel.y > 0 ? targetVel.y * timeOfFlight : 0);
        double predZ = targetEyePos.z + targetVel.z * timeOfFlight;

        return new Vec3(predX, predY, predZ);
    }

    private float calculateBallisticPitch(float distXZ, float distY, float velocity, float gravity) {
        double v2 = velocity * velocity;
        double v4 = v2 * v2;
        double g = gravity;
        double x = distXZ;
        double y = distY;

        double root = v4 - g * (g * x * x + 2.0 * y * v2);
        if (root < 0) {
            // Cannot reach target with ballistics, fall back to direct angle
            return (float) -Math.toDegrees(Math.atan2(distY, distXZ));
        }

        double tanTheta = (v2 - Math.sqrt(root)) / (g * x);
        return (float) -Math.toDegrees(Math.atan(tanTheta));
    }

    private Entity findTarget() {
        Entity bestTarget = null;
        double bestDist = range.getValue();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!isValidTarget(entity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist < bestDist) {
                bestDist = dist;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player || !entity.isAlive() || entity.isSpectator()) return false;
        if (!(entity instanceof LivingEntity)) return false;

        double dist = mc.player.distanceTo(entity);
        if (dist > range.getValue()) return false;

        boolean isPlayer = entity instanceof Player;
        boolean isMob = entity instanceof Enemy;
        boolean isAnimal = entity instanceof Animal;

        if (isPlayer && !targetPlayers.isOn()) return false;
        if (isMob && !targetMobs.isOn()) return false;
        if (isAnimal && !targetAnimals.isOn()) return false;
        if (!isPlayer && !isMob && !isAnimal && !targetMobs.isOn()) return false;

        if (fov.getValue() < 360.0) {
            double diffX = entity.getX() - mc.player.getX();
            double diffZ = entity.getZ() - mc.player.getZ();
            float targetYaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
            float yawDiff = Mth.wrapDegrees(targetYaw - mc.player.getYRot());
            if (Math.abs(yawDiff) > fov.getValue() / 2.0) {
                return false;
            }
        }

        return true;
    }
}
