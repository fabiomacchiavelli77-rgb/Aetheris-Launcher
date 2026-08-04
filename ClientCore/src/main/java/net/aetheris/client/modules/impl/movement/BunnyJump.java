package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class BunnyJump extends Module {

    public BunnyJump() {
        super("BunnyJump", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.level != null) {
            if (mc.player.onGround() && !mc.player.isPassenger() && !mc.player.isSpectator()) {
                if (mc.player.input != null && (mc.player.input.hasForwardImpulse() || mc.player.input.keyPresses.left() || mc.player.input.keyPresses.right() || mc.player.input.keyPresses.backward())) {
                    mc.player.jumpFromGround();
                }
            }
        }
    }
}
