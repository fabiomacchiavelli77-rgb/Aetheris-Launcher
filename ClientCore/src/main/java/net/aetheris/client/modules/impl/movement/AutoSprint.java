package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;

public class AutoSprint extends Module {
    private final BooleanSetting keepSprinting = new BooleanSetting("keepSprinting", "Keep Sprinting", "Mantieni Sprint", true);

    public AutoSprint() {
        super("AutoSprint", Category.MOVEMENT);
        addSetting(keepSprinting);
    }

    @Override
    public void onTick() {
        if (mc.player != null && !mc.player.horizontalCollision && mc.player.getSpeed() > 0) {
            mc.player.setSprinting(keepSprinting.isOn());
        }
    }
}
