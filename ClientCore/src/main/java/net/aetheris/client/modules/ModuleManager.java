package net.aetheris.client.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    /** Indice classi -> modulo per lookup O(1) negli hot-path (render/tick/rete). */
    private static final Map<Class<?>, Module> classIndex = new ConcurrentHashMap<>();

    public static void init() {
        // === COMBAT (14) ===
        addModule(new KillAura());
        addModule(new Velocity());
        addModule(new Criticals());
        addModule(new Reach());
        addModule(new AutoArmor());
        addModule(new AutoTotem());
        addModule(new TriggerBot());
        addModule(new Surround());
        addModule(new AimAssist());
        addModule(new BowAimbot());
        addModule(new SelfTrap());
        addModule(new BedAura());
        addModule(new BedTrap());
        addModule(new CrystalAura());

        // === MOVEMENT (12) ===
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
        addModule(new AutoWalk());
        addModule(new Gesu());

        // === RENDER (13) ===
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
        addModule(new Waypoints());
        addModule(new SpectatorDetector());

        // === WORLD (15) ===
        addModule(new FastBreak());
        addModule(new Scaffold());
        addModule(new Timer());
        addModule(new AutoTool());
        addModule(new InstalledPlugins());
        addModule(new LiquidInteract());
        addModule(new AutoSign());
        addModule(new AutoFarm());
        addModule(new AirPlace());
        addModule(new AutoBrewer());
        addModule(new AutoSmelter());
        addModule(new StrongholdFinder());
        addModule(new PacketLogger());
        addModule(new ServerFinder());
        addModule(new PluginScanner());

        // === PLAYER (12) ===
        addModule(new AutoRespawn());
        addModule(new FastPlace());
        addModule(new NoHunger());
        addModule(new ChestStealer());
        addModule(new AutoFish());
        addModule(new InventoryCleaner());
        addModule(new AntiAFK());
        addModule(new AutoEat());
        addModule(new InventorySort());
        addModule(new AntiDetect());
        addModule(new NoChatReports());
        addModule(new PermissionViewer());

        // === SEEDCRACKER (1) ===
        addModule(new SeedCrackerModule());
    }

    public static void addModule(Module module) {
        modules.add(module);
        classIndex.put(module.getClass(), module);
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

    /** Lookup diretto per classe O(1): usato dai mixin hot-path (render/tick/rete). */
    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(Class<T> clazz) {
        Module cached = classIndex.get(clazz);
        if (cached != null) {
            return (T) cached;
        }
        // Fallback lineare (es. sottoclassi registrate con classe diversa)
        for (Module m : modules) {
            if (clazz.isInstance(m)) {
                classIndex.putIfAbsent(clazz, m);
                return (T) m;
            }
        }
        return null;
    }

    public static void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }
}
