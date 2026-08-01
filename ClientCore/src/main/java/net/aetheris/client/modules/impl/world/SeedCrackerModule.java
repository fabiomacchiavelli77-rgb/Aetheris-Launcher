package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import kaptainwutax.seedcrackerX.config.Config;
import kaptainwutax.seedcrackerX.finder.FinderQueue;

public class SeedCrackerModule extends Module {

    public SeedCrackerModule() {
        super("SeedCracker", "Finds world seed from world structures and decorators.", Category.SEEDCRACKER);
    }

    @Override
    public void onEnable() {
        Config.get().active = true;
    }

    @Override
    public void onDisable() {
        Config.get().active = false;
        FinderQueue.get().clear();
    }
}
