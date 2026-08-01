package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

public class Step extends Module {
    private final SliderSetting stepHeight = new SliderSetting("stepHeight", "Step Height", "Altezza Step", 1.5, 1.0, 10.0, 0.5, "blocks");
    private float oldStepHeight;

    public Step() {
        super("Step", "Sale automaticamente blocchi.", Category.MOVEMENT);
        addSetting(stepHeight);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            oldStepHeight = mc.player.maxUpStep();
            mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(stepHeight.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(oldStepHeight);
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.maxUpStep() != stepHeight.getValue()) {
            mc.player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT).setBaseValue(stepHeight.getValue());
        }
    }
}
