package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FullBright extends Module {

    private final SliderSetting brightness = new SliderSetting("brightness", "Brightness", "Luminosità", 1000.0, 100.0, 1000.0, 50.0, "%");

    public FullBright() {
        super("FullBright", Category.RENDER);
        addSetting(brightness);
    }

    @Override
    public void onTick() {
        if (mc.player != null) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 520, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
