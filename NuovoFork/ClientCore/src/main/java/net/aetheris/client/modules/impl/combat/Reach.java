package net.aetheris.client.modules.impl.combat;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class Reach extends Module {
    private float reachDistance = 5.0f;

    public Reach() {
        super("Reach", "Estende la distanza di attacco e interazione.", Category.COMBAT);
    }

    public float getReachDistance() { return reachDistance; }
    public void setReachDistance(float dist) { this.reachDistance = Math.min(dist, 6.0f); }
}
