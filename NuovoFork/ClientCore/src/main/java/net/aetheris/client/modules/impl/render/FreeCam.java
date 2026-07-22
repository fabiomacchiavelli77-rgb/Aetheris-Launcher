package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.phys.Vec3;

public class FreeCam extends Module {
    private Vec3 savedPosition;
    private float savedYaw, savedPitch;

    public FreeCam() {
        super("FreeCam", "Stacca la telecamera dal player in modalità spettatore.", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        savedPosition = mc.player.position();
        savedYaw = mc.player.getYRot();
        savedPitch = mc.player.getXRot();

        // Abilita la modalità spettatore per la camera
        mc.player.setNoGravity(true);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (savedPosition != null) {
            mc.player.setPos(savedPosition);
            mc.player.setYRot(savedYaw);
            mc.player.setXRot(savedPitch);
        }
        mc.player.setNoGravity(false);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.setNoGravity(true);
        // La camera può volare liberamente
        mc.player.getAbilities().mayfly = true;
        mc.player.getAbilities().flying = true;
    }
}
