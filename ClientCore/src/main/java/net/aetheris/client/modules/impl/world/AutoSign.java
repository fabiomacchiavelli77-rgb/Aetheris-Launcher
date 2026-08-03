package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;

public class AutoSign extends Module {

    private static final String[] LINES = { "Aetheris Client", "", "", "" };

    public AutoSign() {
        super("AutoSign", "Compila e conferma automaticamente i cartelli piazzati.", Category.WORLD);
    }

    public String getLine(int index) {
        if (index < 0 || index >= LINES.length) return null;
        return LINES[index];
    }
}
