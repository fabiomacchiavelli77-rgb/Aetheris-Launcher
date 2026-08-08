package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.world.phys.Vec3;

public class Speed extends Module {
    private final SliderSetting multiplier = new SliderSetting("multiplier", "Multiplier", "Moltiplicatore", 1.5, 0.1, 5.0, 0.1, "x");
    private final BooleanSetting autoJump = new BooleanSetting("autoJump", "Auto Jump", "Salto Automatico", true);
    private final BooleanSetting inWater = new BooleanSetting("inWater", "In Water", "In Acqua", false);

    public Speed() {
        super("Speed", "Aumenta la velocità di movimento.", Category.MOVEMENT);
        addSetting(multiplier);
        addSetting(autoJump);
        addSetting(inWater);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!isMoving()) return;
        if (!mc.player.onGround()) return;
        if (mc.player.isInWater() && !inWater.isOn()) return;

        Vec3 delta = mc.player.getDeltaMovement();
        double yaw = Math.toRadians(mc.player.getYRot());

        float forward = (mc.player.input.keyPresses.forward() ? 1f : 0f) - (mc.player.input.keyPresses.backward() ? 1f : 0f);
        float sideways = (mc.player.input.keyPresses.left() ? 1f : 0f) - (mc.player.input.keyPresses.right() ? 1f : 0f);

        double x = -Math.sin(yaw) * forward - Math.cos(yaw) * sideways;
        double z = Math.cos(yaw) * forward - Math.sin(yaw) * sideways;

        double len = Math.sqrt(x * x + z * z);
        if (len > 0) {
            x = x / len * multiplier.getValue() * 0.2;
            z = z / len * multiplier.getValue() * 0.2;
            mc.player.setDeltaMovement(x, delta.y, z);
        }
        if (mc.player.horizontalCollision && autoJump.isOn()) mc.player.jumpFromGround();
    }

    private boolean isMoving() {
        return mc.player != null &&
               (mc.player.input.keyPresses.forward() || mc.player.input.keyPresses.backward() || mc.player.input.keyPresses.left() || mc.player.input.keyPresses.right());
    }
}
