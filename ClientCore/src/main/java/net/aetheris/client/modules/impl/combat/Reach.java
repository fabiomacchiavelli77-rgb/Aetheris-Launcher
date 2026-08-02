package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

import net.minecraft.world.entity.ai.attributes.Attributes;

public class Reach extends Module {
    
    private final SliderSetting combatReach = new SliderSetting("combatReach", "Combat Reach", "Portata Combattimento", 5.0, 3.0, 10.0, 0.5, "blocks");
    private final SliderSetting blockReach = new SliderSetting("blockReach", "Block Reach", "Portata Blocchi", 7.0, 4.5, 12.0, 0.5, "blocks");

    public Reach() {
        super("Reach", "Estende la distanza di attacco e interazione fino a 10-12 blocchi.", Category.COMBAT);
        addSetting(combatReach);
        addSetting(blockReach);
    }

    public float getReachDistance() { return combatReach.getValue().floatValue(); }

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
}
