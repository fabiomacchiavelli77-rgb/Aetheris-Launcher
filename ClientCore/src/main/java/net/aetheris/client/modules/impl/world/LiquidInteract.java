package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class LiquidInteract extends Module {

    public LiquidInteract() {
        super("LiquidInteract", "Permette di piazzare blocchi sopra acqua e lava.", Category.WORLD);
    }

    // La logica è nel mixin BlockItemMixin (override di BlockItem.canPlace)
}
