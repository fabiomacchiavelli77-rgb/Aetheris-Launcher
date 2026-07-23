package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

import net.minecraft.world.entity.ai.attributes.Attributes;

public class Reach extends Module {
    private float reachDistance = 5.0f;

    public Reach() {
        super("Reach", "Estende la distanza di attacco e interazione.", Category.COMBAT);
    }

    public float getReachDistance() { return reachDistance; }
    public void setReachDistance(float dist) { this.reachDistance = Math.min(dist, 6.0f); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        var entityRange = mc.player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var blockRange = mc.player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (entityRange != null) entityRange.setBaseValue(reachDistance);
        if (blockRange != null) blockRange.setBaseValue(reachDistance);
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
