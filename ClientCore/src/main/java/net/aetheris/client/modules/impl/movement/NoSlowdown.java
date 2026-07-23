package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class NoSlowdown extends Module {

    public NoSlowdown() {
        super("NoSlowdown", "Nessun rallentamento quando mangi/blocchi/usi arco.", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Il mixin nel LocalPlayer impedisce il flag isUsingItem che rallenta
        // Il modulo forza l'input a non essere influenzato dall'uso oggetti
        if (mc.player.isUsingItem()) {
            mc.player.input.leftImpulse = mc.player.input.leftImpulse * 0.2f;
            mc.player.input.forwardImpulse = mc.player.input.forwardImpulse * 0.2f;
        }
    }
}
