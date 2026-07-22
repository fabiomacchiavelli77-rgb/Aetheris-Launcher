package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class Tracers extends Module {

    public Tracers() {
        super("Tracers", "Disegna linee verso le entità vicine.", Category.RENDER);
    }

    /**
     * Tracers disegna linee dal centro dello schermo alle entità.
     * Renderizzato nel WorldRenderer dopo il rendering principale.
     * Il rendering effettivo è gestito nel mixin LevelRenderer.
     */
    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
