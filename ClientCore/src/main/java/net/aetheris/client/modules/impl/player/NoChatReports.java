package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;

public class NoChatReports extends Module {

    private final BooleanSetting stripSignatures = new BooleanSetting("stripSignatures", "Strip Signatures", "Rimuovi Firme", true);

    public NoChatReports() {
        super("NoChatReports", "Rimuove le firme dai messaggi di chat inviati.", Category.PLAYER);
        addSetting(stripSignatures);
    }

    public boolean isStripSignatures() {
        return stripSignatures.isOn();
    }
}
