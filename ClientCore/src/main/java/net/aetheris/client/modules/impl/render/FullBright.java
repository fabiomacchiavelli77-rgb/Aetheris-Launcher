package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.client.OptionInstance;

public class FullBright extends Module {
    private double oldGamma;

    public FullBright() {
        super("FullBright", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            OptionInstance<Double> gamma = mc.options.gamma();
            oldGamma = gamma.get();
            gamma.set(100.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.gamma().set(oldGamma);
        }
    }
}
