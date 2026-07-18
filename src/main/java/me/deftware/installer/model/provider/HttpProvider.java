package me.deftware.installer.model.provider;

import me.deftware.installer.Utils;
import me.deftware.installer.model.Manifest;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HttpProvider implements Provider {

    private final static List<String> excludedFiles = Collections.singletonList("readme.txt");

    private final Map<String, String> queries = new HashMap<>();
    private final Manifest.Version version;

    public HttpProvider(Manifest.Version version) {
        this.version = version;
    }

    @Override
    public Provider withDonor(boolean state) {
        if (state) {
            queries.put("donor", "true");
        }
        return this;
    }

    @Override
    public Provider withLauncher(String name) {
        queries.put("launcher", name);
        return this;
    }

    @Override
    public Provider withProfile(String name) {
        queries.put("profile", name);
        return this;
    }

    public InputStream getStream() throws IOException {
        return Utils.loadWebResource(this.build());
    }

    @Override
    public String getVersion() {
        return version.getId();
    }

    @Override
    public int getProtocol() {
        return version.getProtocol();
    }

    @Override
    public String getProfile() {
        return queries.getOrDefault("profile", getVersion() + "-Aristois");
    }

    @Override
    public String getSource() {
        return this.build();
    }

    @Override
    public void install(Path directory, Consumer<String> logger) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        // Download
        logger.accept("Downloading archive...");
        Path temp = directory.resolve("temp.zip");
        try (InputStream stream = getStream()) {
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
        }
        logger.accept("Downloaded " + FileUtils.byteCountToDisplaySize(Files.size(temp)));
        // Extract
        try (ZipFile zipFile = new ZipFile(temp.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (excludedFiles.contains(name)) {
                    continue;
                }
                Path dest = directory.resolve(name);
                if (entry.isDirectory()) {
                    if (!Files.exists(dest)) {
                        Files.createDirectories(dest);
                    }
                    continue;
                }
                logger.accept("Extracting " + entry.getName());
                try (InputStream stream = zipFile.getInputStream(entry)) {
                    Files.copy(stream, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        Files.deleteIfExists(temp);
    }

    private String encode(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException("Unable to encode string " + text);
        }
    }

    public String build() {
        StringBuilder builder = new StringBuilder("https://maven.aristois.net/manifest/" + getVersion() + ".zip");
        String query = queries.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        if (!query.isEmpty()) {
            builder.append("?").append(query);
        }
        return builder.toString();
    }

}
