package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public class ESP extends Module {

    public ESP() {
        super("ESP", "Evidenzia le entità attraverso i muri.", Category.RENDER);
    }

    public void renderESP(Matrix4f matrix, Camera camera) {
        if (mc.level == null) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!entity.isAlive()) continue;
            if (entity instanceof Player || entity instanceof LivingEntity) {
                entity.setGlowingTag(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity.isCurrentlyGlowing()) entity.setGlowingTag(false);
            }
        }
    }
}
