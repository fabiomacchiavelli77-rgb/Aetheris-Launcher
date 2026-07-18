package me.deftware.installer.model.launcher;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import me.deftware.installer.Utils;
import me.deftware.installer.model.provider.Provider;
import org.lwjgl.system.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrismLauncher implements Launcher {

    private final Function<Path, Path> resolver = path -> path.resolve("instances");

    @Override
    public String getId() {
        return "multimc";
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
        if (Platform.get() == Platform.MACOSX) {
            return Arrays.asList(
                    Paths.get("/", "Applications", "MultiMC.app", "Data"),
                    AppSupport.resolve("PrismLauncher")
            );
        }
        return Collections.emptyList();
    }

    @Override
    public String getDisplayName() {
        return "MultiMC/Prism";
    }

    @Override
    public Path getMinecraftDirectory(Path root, String name) {
        return resolver.apply(root).resolve(name).resolve(".minecraft");
    }

    /**
     * @param provider Provider for version matching
     * @param requiredComponents Required components
     * @return Instances matching the requirements
     */
    public List<String> getInstances(Path root, Provider provider, String... requiredComponents) throws IOException {
        List<String> instances = new ArrayList<>();
        Path instanceDir = resolver.apply(root);

        if (Files.exists(instanceDir)) {
            try (Stream<Path> files  = Files.list(instanceDir)) {
                for (Path file : files.collect(Collectors.toList())) {
                    Path mmc = file.resolve("mmc-pack.json");
                    if (Files.exists(mmc)) {
                        try (InputStream stream = Files.newInputStream(mmc);
                             Reader reader = new InputStreamReader(stream)) {
                            JsonObject json = Utils.GSON.fromJson(reader, JsonObject.class);
                            Map<String, Component> components = Arrays.stream(
                                    Utils.GSON.fromJson(json.getAsJsonArray("components"), Component[].class)
                            ).collect(Collectors.toMap(Component::getUid, Function.identity()));

                            Component minecraft = components.get("net.minecraft");
                            if (minecraft == null || !minecraft.getVersion().equals(provider.getVersion())) {
                                continue;
                            }

                            int count = 0;
                            for (String required : requiredComponents) {
                                if (components.containsKey(required)) {
                                    count++;
                                }
                            }

                            if (count != requiredComponents.length) {
                                continue;
                            }

                            instances.add(file.getFileName().toString());
                        }
                    }
                }
            }
        }

        return instances;
    }

    private static class Component {

        @SerializedName("uid")
        private String uid;

        @SerializedName("version")
        private String version;

        public String getUid() {
            return uid;
        }

        public String getVersion() {
            return version;
        }

    }

}
