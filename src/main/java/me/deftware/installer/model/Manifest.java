package me.deftware.installer.model;

import com.google.gson.annotations.SerializedName;
import me.deftware.installer.model.launcher.Launcher;
import me.deftware.installer.model.launcher.PrismLauncher;
import me.deftware.installer.model.launcher.VanillaLauncher;
import me.deftware.installer.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class Manifest {

    private static Manifest instance;

    @SerializedName("launchers")
    private List<Launchers> launchers;

    @SerializedName("versions")
    private List<Version> versions;

    public List<Launchers> getLaunchers() {
        return launchers;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public static Manifest init() throws IOException {
        try (
                InputStream stream = Utils.loadWebResource("https://maven.aristois.net/manifest");
                Reader reader = new InputStreamReader(stream)
        ) {
            instance = Utils.GSON.fromJson(reader, Manifest.class);
            return instance;
        }
    }

    public static Manifest getInstance() {
        return instance;
    }

    public static class Version {

        @SerializedName("id")
        private String id;

        @SerializedName("protocol")
        private int protocol;

        @SerializedName("platform")
        private String platform;

        @SerializedName("loaders")
        private List<Loader> loaders;

        public String getId() {
            return id;
        }

        public int getProtocol() {
            return protocol;
        }

        public String getPlatform() {
            return platform;
        }

        public List<Loader> getLoaders() {
            return loaders;
        }

        public boolean isForgeSupported() {
            return loaders != null && loaders.contains(Loader.forge);
        }

    }

    public enum Loader {
        forge
    }

    public enum Launchers {
        vanilla(new VanillaLauncher()),
        multimc(new PrismLauncher());

        private final Launcher launcher;

        Launchers(Launcher launcher) {
            this.launcher = launcher;
        }

        public Launcher getLauncher() {
            return launcher;
        }
    }

}
