package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class NameTags extends Module {

    public NameTags() {
        super("NameTags", "Aumenta la dimensione dei nametag e mostra vita.", Category.RENDER);
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
