package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class Timer extends Module {
    private float timerSpeed = 1.5f;

    public Timer() {
        super("Timer", "Accelera il tick rate del client.", Category.WORLD);
    }

    public float getTimerSpeed() { return isEnabled() ? timerSpeed : 1.0f; }
    public void setTimerSpeed(float speed) { this.timerSpeed = Math.min(Math.max(speed, 0.1f), 10.0f); }
}
