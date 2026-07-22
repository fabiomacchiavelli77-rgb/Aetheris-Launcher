package net.aetheris.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.aetheris.client.modules.impl.combat.*;
import net.aetheris.client.modules.impl.movement.*;
import net.aetheris.client.modules.impl.render.*;
import net.aetheris.client.modules.impl.world.*;
import net.aetheris.client.modules.impl.player.*;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // === COMBAT (8) ===
        addModule(new KillAura());
        addModule(new Velocity());
        addModule(new Criticals());
        addModule(new Reach());
        addModule(new AutoArmor());
        addModule(new AutoTotem());
        addModule(new TriggerBot());
        addModule(new Surround());

        // === MOVEMENT (7) ===
        addModule(new AutoSprint());
        addModule(new Speed());
        addModule(new Fly());
        addModule(new NoFall());
        addModule(new Step());
        addModule(new NoSlowdown());
        addModule(new NoClip());

        // === RENDER (7) ===
        addModule(new FullBright());
        addModule(new ESP());
        addModule(new NoHurtCam());
        addModule(new Xray());
        addModule(new NameTags());
        addModule(new Tracers());
        addModule(new FreeCam());

        // === WORLD (4) ===
        addModule(new FastBreak());
        addModule(new Scaffold());
        addModule(new Timer());
        addModule(new AutoTool());

        // === PLAYER (6) ===
        addModule(new AutoRespawn());
        addModule(new FastPlace());
        addModule(new NoHunger());
        addModule(new ChestStealer());
        addModule(new AutoFish());
        addModule(new InventoryCleaner());
    }

    public static void addModule(Module module) {
        modules.add(module);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static Optional<Module> getModule(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
