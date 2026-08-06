package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class CameraClip extends Module {

    private final SliderSetting distance = new SliderSetting("distance", "Distance", "Distanza", 4.0, 1.0, 20.0, 0.5, "blocks");

    public CameraClip() {
        super("CameraClip", "Permette alla visuale in terza persona di attraversare i blocchi senza avvicinarsi.", Category.RENDER);
        addSetting(distance);
    }

    public float getDistance() {
        return (float) distance.getValue().doubleValue();
    }
}
