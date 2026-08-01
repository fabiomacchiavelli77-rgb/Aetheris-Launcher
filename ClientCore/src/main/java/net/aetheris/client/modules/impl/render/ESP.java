package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public class ESP extends Module {

    private final BooleanSetting players = new BooleanSetting("players", "Players", "Giocatori", true);
    private final BooleanSetting mobs = new BooleanSetting("mobs", "Mobs", "Mob Ostili", false);
    private final BooleanSetting items = new BooleanSetting("items", "Items", "Oggetti", true);
    private final BooleanSetting storage = new BooleanSetting("storage", "Storage", "Contenitori", true);
    private final SliderSetting lineWidth = new SliderSetting("lineWidth", "Line Width", "Spessore Linea", 1.5, 1.0, 5.0, 0.5, "");

    public ESP() {
        super("ESP", "Evidenzia le entità attraverso i muri.", Category.RENDER);
        addSetting(players);
        addSetting(mobs);
        addSetting(items);
        addSetting(storage);
        addSetting(lineWidth);
    }

    public void renderESP(Matrix4f matrix, Camera camera) {
        if (mc.level == null) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!entity.isAlive()) continue;
            
            boolean shouldGlow = false;
            if (entity instanceof Player && players.isOn()) {
                shouldGlow = true;
            } else if (entity instanceof LivingEntity && !(entity instanceof Player) && mobs.isOn()) {
                shouldGlow = true;
            }
            // Logic for items/storage would go here when implemented in client
            // We maintain the existing entity support.
            
            if (shouldGlow) {
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
