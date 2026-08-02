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

    public boolean shouldGlow(Entity entity) {
        if (entity == mc.player) return false;
        if (!entity.isAlive()) return false;
        
        if (entity instanceof Player && players.isOn()) {
            return true;
        } else if (entity instanceof LivingEntity && !(entity instanceof Player) && mobs.isOn()) {
            return true;
        }
        
        // Items and storage support to be added
        return false;
    }
}
