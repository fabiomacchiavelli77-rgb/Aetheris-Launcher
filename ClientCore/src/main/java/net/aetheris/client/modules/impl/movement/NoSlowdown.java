package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;

public class NoSlowdown extends Module {
    private final BooleanSetting items = new BooleanSetting("items", "Eat/Drink", "Mangiare/Bere", true);
    private final BooleanSetting soulSand = new BooleanSetting("soulSand", "Soul Sand", "Sabbia delle Anime", true);
    private final BooleanSetting honeyBlock = new BooleanSetting("honeyBlock", "Honey Block", "Blocco di Miele", true);
    private final BooleanSetting cobweb = new BooleanSetting("cobweb", "Cobweb", "Ragnatela", true);

    public NoSlowdown() {
        super("NoSlowdown", "Nessun rallentamento quando mangi/blocchi/usi arco.", Category.MOVEMENT);
        addSetting(items);
        addSetting(soulSand);
        addSetting(honeyBlock);
        addSetting(cobweb);
    }

    public boolean getItems() {
        return items.isOn();
    }

    @Override
    public void onTick() {
    }
}
