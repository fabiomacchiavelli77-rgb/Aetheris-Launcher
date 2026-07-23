package me.deftware.installer.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.deftware.installer.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Patches mod metadata inside JAR files to evade
 * signature-based anti-cheat detection.
 */
public class JarPatcher {

    /**
     * Patches fabric.mod.json and/or mcmod.info inside a JAR file.
     * Appends a random suffix to mod IDs and obfuscates the mod name.
     *
     * @param jarPath Path to the JAR file to patch
     * @return true if the JAR was modified
     * @throws IOException if file operations fail
     */
    public static boolean patch(Path jarPath) throws IOException {
        Path tempPath = jarPath.resolveSibling(jarPath.getFileName() + ".tmp");
        boolean modified = false;

        try (ZipFile zip = new ZipFile(jarPath.toFile());
             ZipOutputStream zos = new ZipOutputStream(
                 new BufferedOutputStream(Files.newOutputStream(tempPath)))) {

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.equals("fabric.mod.json") || name.equals("mcmod.info")) {
                    byte[] content;
                    try (InputStream is = zip.getInputStream(entry);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = is.read(buf)) > 0) {
                            baos.write(buf, 0, len);
                        }
                        content = baos.toByteArray();
                    }

                    byte[] patched = patchMetadata(name, content);
                    modified = true;

                    ZipEntry newEntry = new ZipEntry(name);
                    newEntry.setTime(entry.getTime());
                    zos.putNextEntry(newEntry);
                    zos.write(patched);
                    zos.closeEntry();
                } else {
                    zos.putNextEntry(new ZipEntry(entry));
                    try (InputStream is = zip.getInputStream(entry)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = is.read(buf)) > 0) {
                            zos.write(buf, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
            }
        }

        if (modified) {
            Files.move(tempPath, jarPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(tempPath);
        }
        return modified;
    }

    private static byte[] patchMetadata(String entryName, byte[] content) {
        String json = new String(content, StandardCharsets.UTF_8);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        String suffix = UUID.randomUUID().toString().substring(0, 6);

        if (entryName.equals("fabric.mod.json")) {
            if (root.has("id")) {
                String orig = root.get("id").getAsString();
                root.addProperty("id", orig + "-" + suffix);
            }
            if (root.has("name")) {
                String orig = root.get("name").getAsString();
                root.addProperty("name", orig + " (Optimization)");
            }
        } else if (entryName.equals("mcmod.info")) {
            if (root.isJsonArray() && root.getAsJsonArray().size() > 0) {
                JsonObject mod = root.getAsJsonArray().get(0).getAsJsonObject();
                if (mod.has("modid")) {
                    mod.addProperty("modid", mod.get("modid").getAsString() + "-" + suffix);
                }
            } else if (root.has("modid")) {
                root.addProperty("modid", root.get("modid").getAsString() + "-" + suffix);
            }
        }

        return Utils.GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
    }
}
