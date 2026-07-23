package me.deftware.installer.model.launcher;

import me.deftware.installer.Main;
import me.deftware.installer.Utils;
import me.deftware.installer.model.process.LauncherProcess;
import me.deftware.installer.model.process.WindowsProcess;
import me.deftware.installer.model.provider.Provider;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public interface Launcher {

    /**
     * macOS Application Support directory
     */
    Path AppSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support");

    /**
     * @return The launcher id
     */
    String getId();

    /**
     * @param path Path to test
     * @return If a given path is a launcher dir
     */
    boolean isValidPath(Path path);

    /**
     * @param path Root dir
     * @return The extraction path relative to the root dir
     */
    Path getInstallationDirectory(Path path);

    default Path getMinecraftDirectory(Path root, String profile) {
        return root;
    }

    default Path getModsDirectory(Path root, String profile) {
        return getMinecraftDirectory(root, profile).resolve("mods");
    }

    default Path getEMCDirectory(Path root, String profile) {
        return getMinecraftDirectory(root, profile).resolve("libraries").resolve("EMC");
    }

    /**
     * @return A list of common installation directories
     */
    List<Path> getCommonPaths();

    /**
     * @return The launcher name
     */
    String getDisplayName();

    default Optional<String> getLauncherBinary() {
        return Optional.empty();
    }

    default Optional<LauncherProcess> getProcess() {
        if (Platform.get() == Platform.WINDOWS) {
            Optional<String> binary = getLauncherBinary();
            if (binary.isPresent()) {
                return Optional.of(new WindowsProcess(binary.get()));
            }
        }
        return Optional.empty();
    }

    /**
     * Run post-install configurations for the launcher
     */
    default void configure(Provider provider, Path root) throws IOException {
        // Nothing
    }

    default void clean(Provider provider, Path root, String profile) throws IOException {
        Path libraries = root.resolve("libraries");
        Path deftware = libraries.resolve("me").resolve("deftware");
        if (Files.exists(deftware)) {
            Utils.delete(deftware);
        }

        Path emc = getEMCDirectory(root, profile).resolve(provider.getVersion());
        if (Files.exists(emc)) {
            Utils.delete(emc);
        }
    }

    static String getBase64Icon() throws IOException {
        try (InputStream stream = Main.class.getResourceAsStream("/logo.png")) {
            if (stream == null) {
                throw new IOException("Unable to read logo");
            }
            byte[] bytes = IOUtils.toByteArray(stream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        }
    }

    default boolean isVanilla() {
        return this instanceof VanillaLauncher;
    }

}
