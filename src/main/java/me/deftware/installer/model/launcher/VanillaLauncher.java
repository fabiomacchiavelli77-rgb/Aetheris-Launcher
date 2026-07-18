package me.deftware.installer.model.launcher;

import com.google.gson.JsonObject;
import me.deftware.installer.Utils;
import me.deftware.installer.model.provider.Provider;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class VanillaLauncher implements Launcher {

    private final Function<Path, Path> resolver = path -> path.resolve("versions");

    @Override
    public String getId() {
        return "vanilla";
    }

    @Override
    public boolean isValidPath(Path path) {
        return Files.exists(resolver.apply(path));
    }

    @Override
    public Path getInstallationDirectory(Path path) {
        return resolver.apply(path);
    }

    @Override
    public List<Path> getCommonPaths() {
        switch (Platform.get()) {
            case WINDOWS:
                return Collections.singletonList(Paths.get(System.getenv("APPDATA"), ".minecraft"));
            case MACOSX:
                return Collections.singletonList(AppSupport.resolve("minecraft"));
            default:
                // Other unix-like types
                return Arrays.asList(
                        Paths.get(System.getProperty("user.home"), ".minecraft"),
                        // Flatpack
                        Paths.get(System.getProperty("user.home"), ".var", "app", "com.mojang.Minecraft", ".minecraft")
                );
        }
    }

    @Override
    public String getDisplayName() {
        return "Vanilla";
    }

    @Override
    public Optional<String> getLauncherBinary() {
        if (Platform.get() == Platform.WINDOWS) {
            return Optional.of("MinecraftLauncher.exe");
        }
        return Optional.empty();
    }

    @Override
    public void configure(Provider provider, Path root) throws IOException {
        String version = provider.getVersion();
        // Create vanilla version
        PistonMeta pistonMeta = PistonMeta.getInstance();
        if (pistonMeta != null) {
            pistonMeta.install(resolver.apply(root), version);
        }
        // Add user profile
        Path profilesFile = root.resolve("launcher_profiles.json");
        if (Files.exists(profilesFile)) {
            try (Reader reader = Files.newBufferedReader(profilesFile)) {
                JsonObject json = Utils.GSON.fromJson(reader, JsonObject.class);
                JsonObject profiles = json.getAsJsonObject("profiles");

                String name = provider.getProfile();
                JsonObject profile = getProfile(name);
                profiles.add(name, profile);

                try (Writer writer = Files.newBufferedWriter(profilesFile)) {
                    Utils.GSON.toJson(json, writer);
                }
            }
        }
    }

    private JsonObject getProfile(String name) throws IOException {
        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
        String icon = Launcher.getBase64Icon();

        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("type", "custom");
        json.addProperty("created", date);
        json.addProperty("lastUsed", date);
        json.addProperty("lastVersionId", name);
        json.addProperty("icon", icon);
        return json;
    }

}
