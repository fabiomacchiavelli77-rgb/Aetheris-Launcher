package me.deftware.installer.view.scenes;

import me.deftware.installer.model.Configuration;
import me.deftware.installer.model.JarPatcher;
import me.deftware.installer.model.launcher.Launcher;
import me.deftware.installer.model.process.LauncherProcess;
import me.deftware.installer.model.provider.Provider;
import me.deftware.installer.Utils;
import me.deftware.installer.view.components.Button;
import me.deftware.installer.view.components.Component;
import me.deftware.installer.view.components.ProgressBar;
import me.deftware.installer.view.components.TextRenderer;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.lwjgl.nanovg.NanoVG.*;

public class InstallingScene extends Scene {

    private boolean wasRunning;
    private LauncherProcess process;

    private final Configuration configuration;
    private final Provider provider;
    private final Launcher launcher;
    private final Path root;

    private String subtitle = "Please wait whilst Aristois is being installed";
    private CompletableFuture<Void> thread;
    private ProgressBar progressBar;
    private Log log;

    public InstallingScene(Configuration configuration, Provider provider, Launcher launcher, Path root) {
        this.configuration = configuration;
        this.provider = provider;
        this.launcher = launcher;
        this.root = root;
        Optional<LauncherProcess> instance = launcher.getProcess();
        if (instance.isPresent()) {
            process = instance.get();
            Utils.runThrowable(() -> {
                if (process.isRunning()) {
                    process.stop();
                    wasRunning = true;
                }
            });
        }
        setTitle("Installing Aristois");
    }

    private void run() throws IOException {
        Thread.currentThread().setName("Install thread");

        String profile = provider.getProfile();
        if (configuration.isForge() && !launcher.isVanilla()) {
            profile = configuration.getInstance();
        }

        Consumer<String> logText = line -> {
            progressBar.setLabel(line);
            log.append(line);
        };

        if (configuration.isClean()) {
            logText.accept("Cleaning");
            launcher.clean(provider, root, profile);
        }

        if (configuration.isForge()) {
            provider.forge(root, launcher, profile, logText);
            if (launcher.isVanilla()) {
                log.append("Note: Aristois will not create a profile for Forge installations");
                log.append("Install Forge separately, then run the Forge profile");
            } else {
                log.append("To use Aristois start the " + profile + " instance");
            }
        } else {
            provider.install(launcher.getInstallationDirectory(root), logText);
            logText.accept("Configuring launcher");
            launcher.configure(provider, root);

            if (wasRunning) {
                logText.accept("Starting launcher");
                Utils.runThrowable(process::start);
            }

            log.append("To use Aristois start the " + profile + " profile");
        }

        // Install SeedCrackerX if requested
        if (configuration.isSeedCracker() && !configuration.isForge()) {
            logText.accept("Installing SeedCrackerX");
            try {
                provider.installSeedCracker(root, launcher, profile,
                        provider.getVersion(), configuration.isForge(), logText);
            } catch (Exception ex) {
                log.append("SeedCrackerX install failed: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else if (configuration.isSeedCracker() && configuration.isForge()) {
            log.append("Note: SeedCrackerX is Fabric-only, skipped (Forge install)");
        }

        // Apply anti-detection patches if requested
        if (configuration.isAntiDetection()) {
            logText.accept("Applying anti-detection patches");
            Path installDir;
            if (configuration.isForge()) {
                installDir = launcher.getEMCDirectory(root, profile)
                        .resolve(provider.getVersion());
            } else {
                installDir = launcher.getInstallationDirectory(root);
            }
            try (Stream<Path> walk = Files.walk(installDir)) {
                walk.filter(p -> p.toString().endsWith(".jar")).forEach(jar -> {
                    try {
                        if (JarPatcher.patch(jar)) {
                            logText.accept("Patched " + jar.getFileName());
                        }
                    } catch (Exception ex) {
                        log.append("Patch failed: " + jar.getFileName());
                    }
                });
            } catch (Exception ex) {
                log.append("Anti-detection patch error: " + ex.getMessage());
            }
        }

        // Pre-configure Aristois to skip the welcome/update screens and remove wait times
        logText.accept("Pre-configuring Aristois settings...");
        try {
            Path emcConfigs = launcher.getEMCDirectory(root, profile)
                    .resolve(provider.getVersion())
                    .resolve("configs");
            if (!Files.exists(emcConfigs)) {
                Files.createDirectories(emcConfigs);
            }
            Path aristoisConfig = emcConfigs.resolve("Aristois_config.json");
            com.google.gson.JsonObject configJson = new com.google.gson.JsonObject();
            if (Files.exists(aristoisConfig)) {
                try (java.io.Reader reader = Files.newBufferedReader(aristoisConfig)) {
                    configJson = Utils.GSON.fromJson(reader, com.google.gson.JsonObject.class);
                }
            }
            configJson.addProperty("the_new_welcome_screen", false);
            configJson.addProperty("_social-splash-screen", false);
            configJson.addProperty("aristois__version", 9999);
            configJson.addProperty("shown_gui_usage", true);
            try (java.io.Writer writer = Files.newBufferedWriter(aristoisConfig)) {
                Utils.GSON.toJson(configJson, writer);
            }
            log.append("Bypassed welcome & update screens (0s wait time)");
        } catch (Exception ex) {
            log.append("Pre-config failed: " + ex.getMessage());
        }

        logText.accept("Done");
        subtitle = "Installation complete";

        // Add new components
        new Button( "Close") {
            @Override
            protected boolean onClick(int button) {
                GLFW.glfwSetWindowShouldClose(window.getHandle(), true);
                return true;
            }
        }.withCenteredPosition(window.getHeight() - Button.DefaultHeight - 30).add(this);
        remove(progressBar);
    }

    @Override
    public void init() {
        progressBar = new ProgressBar().withCenteredPosition(window.getHeight() - 80).add(this);
        progressBar.setLabel("Waiting");

        log = new Log(230)
                .withLabel("Log")
                .withCenteredPosition(150)
                .add(this);

        super.init();
        thread = CompletableFuture.runAsync(() -> {
            try {
                run();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);
        TextRenderer.drawText(subtitle, xCenter, 100, 0, 0, true, 22);
    }

    private static class Log extends Component<Log> {

        private static final int WIDTH = 600;
        private final List<String> lines = new CopyOnWriteArrayList<>();

        public Log(int height) {
            super(WIDTH, height);
            backgroundColor = Utils.getColor(Color.white);
        }

        @Override
        public void render(int mouseX, int mouseY) {
            super.render(mouseX, mouseY);

            nvgStrokeColor(vg, backgroundColor);
            nvgBeginPath(vg);
            nvgRoundedRect(vg, x, y, width, height, 5);
            nvgStroke(vg);

            int size = 18;
            int posY = y + size;
            for (String line : lines) {
                TextRenderer.drawText(line, x + 2, posY, 0, 0, false, size);
                posY += size;
            }
        }

        public void append(String line) {
            this.lines.add(line);
        }

    }

}
