package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class FastBreak extends Module {
    private float speedMultiplier = 3.0f;

    public FastBreak() {
        super("FastBreak", "Rompe i blocchi più velocemente.", Category.WORLD);
    }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(float multiplier) { this.speedMultiplier = Math.min(Math.max(multiplier, 1.0f), 10.0f); }
}
