package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;

public class Tracers extends Module {

    private final BooleanSetting tracePlayers = new BooleanSetting("tracePlayers", "Players", "Giocatori", true);
    private final BooleanSetting traceMobs = new BooleanSetting("traceMobs", "Mobs", "Mob", false);
    private final BooleanSetting traceItems = new BooleanSetting("traceItems", "Items", "Oggetti", false);
    private final SliderSetting lineWidth = new SliderSetting("lineWidth", "Line Width", "Spessore Linea", 1.5, 1.0, 5.0, 0.5, "");
    private final BooleanSetting distanceColor = new BooleanSetting("distanceColor", "Distance Color", "Colore Distanza", true);

    public Tracers() {
        super("Tracers", "Disegna linee verso le entità vicine.", Category.RENDER);
        addSetting(tracePlayers);
        addSetting(traceMobs);
        addSetting(traceItems);
        addSetting(lineWidth);
        addSetting(distanceColor);
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
