package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class AutoWalk extends Module {

    private final SliderSetting customSpeed = new SliderSetting("customSpeed", "Walk Speed", "Velocità Camminata", 1.0, 0.1, 5.0, 0.1, "x");

    public AutoWalk() {
        super("AutoWalk", "Fa camminare il giocatore in avanti automaticamente.", Category.MOVEMENT);
        addSetting(customSpeed);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        
        double currentSpeed = customSpeed.getValue();
        if (currentSpeed > 0.0) {
            mc.options.keyUp.setDown(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.keyUp.setDown(false);
        }
    }
}
