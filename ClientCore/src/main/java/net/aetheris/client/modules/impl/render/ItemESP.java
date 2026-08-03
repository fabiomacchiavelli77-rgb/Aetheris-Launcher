package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

public class ItemESP extends Module {
    private final SliderSetting range = new SliderSetting("range", "Range", "Portata", 32.0, 8.0, 64.0, 4.0, "blocks");
    private final BooleanSetting showTrash = new BooleanSetting("showTrash", "Show Trash", "Mostra Spazzatura", false);

    public ItemESP() {
        super("ItemESP", "Evidenzia gli oggetti a terra con l'effetto glow.", Category.RENDER);
        addSetting(range);
        addSetting(showTrash);
    }

    /** Chiamato dal MinecraftClientMixin per ogni entità nel render. */
    public boolean shouldGlow(Entity entity) {
        if (mc.player == null || mc.level == null) return false;
        if (!(entity instanceof ItemEntity item)) return false;
        if (!item.isAlive()) return false;
        if (item.distanceToSqr(mc.player) > range.getValue() * range.getValue()) return false;
        if (!showTrash.isOn() && item.getItem().isEmpty()) return false;
        return true;
    }
}
