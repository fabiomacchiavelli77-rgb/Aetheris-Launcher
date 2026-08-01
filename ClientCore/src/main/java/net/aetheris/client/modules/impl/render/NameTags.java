package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;

public class NameTags extends Module {

    private final SliderSetting scale = new SliderSetting("scale", "Scale", "Scala", 1.0, 0.5, 3.0, 0.1, "x");
    private final BooleanSetting showHealth = new BooleanSetting("showHealth", "Show Health", "Mostra Salute", true);
    private final BooleanSetting showArmor = new BooleanSetting("showArmor", "Show Armor", "Mostra Armatura", true);
    private final BooleanSetting showDistance = new BooleanSetting("showDistance", "Show Distance", "Mostra Distanza", true);

    public NameTags() {
        super("NameTags", "Aumenta la dimensione dei nametag e mostra vita.", Category.RENDER);
        addSetting(scale);
        addSetting(showHealth);
        addSetting(showArmor);
        addSetting(showDistance);
    }

    /**
     * NameTags funziona tramite mixin su EntityRenderer che scala il nametag
     * e aggiunge la barra della vita. Questo modulo è il toggle.
     */
    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
