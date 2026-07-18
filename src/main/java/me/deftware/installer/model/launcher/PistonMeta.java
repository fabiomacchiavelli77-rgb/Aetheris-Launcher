package me.deftware.installer.model.launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.deftware.installer.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PistonMeta {

    private final static Logger logger = LoggerFactory.getLogger("Minecraft|Meta");

    private static PistonMeta instance;

    private final JsonObject manifest;
    private final JsonArray versions;

    private PistonMeta(JsonObject manifest) {
        this.manifest = manifest;
        this.versions = manifest.getAsJsonArray("versions");
    }

    public static PistonMeta init() throws IOException {
        logger.info("Fetching Minecraft metadata");
        try (
                InputStream stream = Utils.loadWebResource("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
                Reader reader = new InputStreamReader(stream)
        ) {
            JsonObject json = Utils.GSON.fromJson(reader, JsonObject.class);
            instance = new PistonMeta(json);
            logger.info("Found {} available versions", instance.versions.size());
            return instance;
        }
    }

    public static PistonMeta getInstance() {
        return instance;
    }

    private JsonObject getVersion(String id) {
        for (JsonElement version : versions) {
            JsonObject obj = version.getAsJsonObject();
            if (obj.get("id").getAsString().equals(id)) {
                return obj;
            }
        }
        return null;
    }

    public void install(Path versions, String id) throws IOException {
        Path dir = versions.resolve(id);
        Path json = dir.resolve(id + ".json");
        if (!Files.exists(json)) {
            if (!Files.exists(dir)) {
                logger.info("Creating {}", dir.toAbsolutePath());
                Files.createDirectories(dir);
            }
            JsonObject data = getVersion(id);
            if (data == null) {
                logger.error("Unable to find version entry for {}", id);
                return;
            }
            // Download
            String url = data.get("url").getAsString();
            logger.info("Downloading {}", url);
            try (InputStream stream = Utils.loadWebResource(url);
                 Reader reader = new InputStreamReader(stream);
                 Writer writer = Files.newBufferedWriter(json)) {
                JsonObject jsonFile = Utils.GSON.fromJson(reader, JsonObject.class);

                logger.info("Writing json to {}", json.toAbsolutePath());
                Utils.GSON.toJson(jsonFile, writer);
            }
        }
    }

}
