package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.finder.FinderQueue;

public class SeedCrackerModule extends Module {

    public SeedCrackerModule() {
        super("SeedCracker", "Finds world seed from world structures and decorators.", Category.SEEDCRACKER);
        if (Config.get() != null) {
            Config.get().active = isEnabled();
        }
    }

    @Override
    public void onEnable() {
        if (Config.get() != null) {
            Config.get().active = true;
        }
    }

    @Override
    public void onDisable() {
        if (Config.get() != null) {
            Config.get().active = false;
        }
        if (FinderQueue.get() != null) {
            FinderQueue.get().clear();
        }
    }
}
