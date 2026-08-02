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
        if (mc.player == null) return;
        // Il mixin nel LocalPlayer impedisce il flag isUsingItem che rallenta
        // Il modulo forza l'input a non essere influenzato dall'uso oggetti
        if (mc.player.isUsingItem() && !items.isOn()) {
            mc.player.input.leftImpulse = mc.player.input.leftImpulse * 0.2f;
            mc.player.input.forwardImpulse = mc.player.input.forwardImpulse * 0.2f;
        }
    }
}
