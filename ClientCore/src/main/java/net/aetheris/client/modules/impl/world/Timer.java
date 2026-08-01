package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class Timer extends Module {
    private final SliderSetting speed = new SliderSetting("speed", "Speed", "Velocità", 2.0, 0.1, 10.0, 0.1, "x");

    public Timer() {
        super("Timer", "Accelera il tick rate del client.", Category.WORLD);
        addSetting(speed);
    }

    public float getTimerSpeed() { return isEnabled() ? speed.getValue().floatValue() : 1.0f; }
}
