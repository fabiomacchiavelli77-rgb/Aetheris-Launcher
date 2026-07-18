package me.deftware.installer.model;

import com.google.gson.annotations.SerializedName;
import me.deftware.installer.Main;
import me.deftware.installer.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Config {

    @SerializedName("version")
    private String version;

    @SerializedName("hash")
    private String hash;

    @SerializedName("donor")
    private String donor;

    private static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            try (InputStream stream = Main.class.getResourceAsStream("/config.json")) {
                if (stream == null) {
                    throw new IOException("Unable to load config");
                }
                try (Reader reader = new InputStreamReader(stream)) {
                    instance = Utils.GSON.fromJson(reader, Config.class);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return instance;
    }

    public String getVersion() {
        return version;
    }

    public String getHash() {
        return hash;
    }

    public boolean isDonor() {
        return true;
    }

}
