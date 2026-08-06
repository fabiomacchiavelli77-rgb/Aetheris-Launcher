package net.aetheris.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.modules.impl.combat.*;
import net.aetheris.client.modules.impl.movement.*;
import net.aetheris.client.modules.impl.render.*;
import net.aetheris.client.modules.impl.world.*;
import net.aetheris.client.modules.impl.player.*;
import net.aetheris.client.modules.impl.world.FastBreak;
import net.aetheris.client.modules.impl.world.Scaffold;
import net.aetheris.client.modules.impl.world.Timer;
import net.aetheris.client.modules.impl.world.AutoTool;
import net.aetheris.client.modules.impl.world.InstalledPlugins;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // === COMBAT (12) ===
        addModule(new KillAura());
        addModule(new Velocity());
        addModule(new Criticals());
        addModule(new Reach());
        addModule(new AutoArmor());
        addModule(new AutoTotem());
        addModule(new TriggerBot());
        addModule(new Surround());
        addModule(new AimAssist());
        addModule(new SelfTrap());
        addModule(new BedAura());
        addModule(new BedTrap());
        addModule(new CrystalAura());

        // === MOVEMENT (10) ===
        addModule(new AutoSprint());
        addModule(new Speed());
        addModule(new Fly());
        addModule(new NoFall());
        addModule(new Step());
        addModule(new NoSlowdown());
        addModule(new NoClip());
        addModule(new BunnyJump());
        addModule(new Jetpack());
        addModule(new Sneak());

        // === RENDER (11) ===
        addModule(new FullBright());
        addModule(new ESP());
        addModule(new NoHurtCam());
        addModule(new Xray());
        addModule(new NameTags());
        addModule(new Tracers());
        addModule(new FreeCam());
        addModule(new ItemESP());
        addModule(new StorageESP());
        addModule(new CameraClip());
        addModule(new Trajectories());

        // === WORLD (9) ===
        addModule(new FastBreak());
        addModule(new Scaffold());
        addModule(new Timer());
        addModule(new AutoTool());
        addModule(new InstalledPlugins());
        addModule(new LiquidInteract());
        addModule(new AutoSign());
        addModule(new AutoFarm());
        addModule(new AirPlace());

        // === PLAYER (9) ===
        addModule(new AutoRespawn());
        addModule(new FastPlace());
        addModule(new NoHunger());
        addModule(new ChestStealer());
        addModule(new AutoFish());
        addModule(new InventoryCleaner());
        addModule(new AntiAFK());
        addModule(new AutoEat());
        addModule(new InventorySort());

        // === SEEDCRACKER (1) ===
        addModule(new SeedCrackerModule());
    }

    public static void addModule(Module module) {
        modules.add(module);
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static List<Module> getModules(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).toList();
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
