package me.deftware.installer.model.provider;

import me.deftware.installer.Main;
import me.deftware.installer.Utils;
import me.deftware.installer.model.Config;
import me.deftware.installer.model.launcher.Launcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public interface Provider {

    Provider withDonor(boolean state);

    Provider withLauncher(String name);

    Provider withProfile(String name);

    int getProtocol();

    String getVersion();

    String getProfile();

    String getSource();

    void install(Path directory, Consumer<String> logger) throws IOException;

    default InputStream getForgeEMC() throws IOException {
        String version = getVersion();
        return Utils.loadWebResource(
            String.format("https://gitlab.com/EMC-Framework/maven/-/raw/master/me/deftware/EMC-Forge/latest-%s/EMC-Forge-latest-%s.jar?inline=false", version, version)
        );
    }

    default InputStream getAristois(boolean donor) throws IOException {
        return Utils.loadWebResource(
            String.format("https://maven.aristois.net/me/deftware/aristois%s/latest/aristois-latest.jar", donor ? "-d" : "")
        );
    }

    default void forge(Path root, Launcher launcher, String profile, Consumer<String> logger) throws IOException {
        Path mods = launcher.getModsDirectory(root, profile);
        String version = getVersion();
        if (getProtocol() <= 573) { // 1.15
            mods = mods.resolve(version);
        }
        if (!Files.exists(mods)) {
            Files.createDirectories(mods);
        }
        logger.accept("Downloading EMC");
        try (InputStream stream = getForgeEMC()) {
            Files.copy(stream, mods.resolve("EMC.jar"), StandardCopyOption.REPLACE_EXISTING);
        }

        Path emc = launcher.getEMCDirectory(root, profile).resolve(version);
        if (!Files.exists(emc)) {
            Files.createDirectories(emc);
        }
        logger.accept("Downloading Aristois");
        try (InputStream stream = getAristois(Config.getInstance().isDonor())) {
            Files.copy(stream, emc.resolve("Aristois.jar"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    default void installSeedCracker(Path root, Launcher launcher,
            String profile, String mcVersion, Consumer<String> logger) throws IOException {
        Path mods = launcher.getModsDirectory(root, profile);
        if (getProtocol() <= 573) {
            mods = mods.resolve(mcVersion);
        }
        if (!Files.exists(mods)) {
            Files.createDirectories(mods);
        }
        logger.accept("Installing Aristois Seed Cracker");
        try (InputStream stream = Main.class.getResourceAsStream("/aristois-seed-cracker.jar")) {
            if (stream == null) {
                logger.accept("Warning: Seed cracker JAR not found in resources");
                return;
            }
            Files.copy(stream, mods.resolve("Aristois-Seed-Cracker.jar"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
