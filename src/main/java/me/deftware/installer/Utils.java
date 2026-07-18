package me.deftware.installer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.BufferUtils.createByteBuffer;

public class Utils {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36";

    public static final String defaultFont = "roboto";
    private static final List<String> fonts = new ArrayList<>();

    public static NVGColor getColor(Color color) {
        NVGColor nvgColor = NVGColor.create();
        setColor(color, nvgColor);
        return nvgColor;
    }

    public static void setColor(Color color, NVGColor nvgColor) {
        setColor(nvgColor,
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
    }

    public static NVGColor setColor(NVGColor nvgColor, float r, float g, float b, float a) {
        nvgColor.r(r);
        nvgColor.g(g);
        nvgColor.b(b);
        nvgColor.a(a);
        return nvgColor;
    }

    public static void blend(NVGColor nvgColor, float ratio, Color... colors) {
        int r = 0, g = 0, b = 0, a = 0;
        for (Color color : colors) {
            r += color.getRed() * ratio;
            g += color.getGreen() * ratio;
            b += color.getBlue() * ratio;
            a += color.getAlpha() * ratio;
        }
        setColor(nvgColor, r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public static ByteBuffer toByteBuffer(byte[] bytes) {
        ByteBuffer buffer = createByteBuffer(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    public static ByteBuffer toByteBuffer(InputStream stream) throws IOException {
        return toByteBuffer(IOUtils.toByteArray(stream));
    }

    public static Resource loadFont(long vg, String name, InputStream stream) throws IOException {
        ByteBuffer buffer = toByteBuffer(stream);
        int handle = NanoVG.nvgCreateFontMem(vg, name, buffer, 0);
        if (handle == -1) {
            throw new IOException("Unable to create font " + name);
        }
        fonts.add(name);
        return new Resource(buffer, handle);
    }

    public static Resource loadImage(long vg, InputStream stream) throws IOException {
        ByteBuffer buffer = toByteBuffer(stream);
        int handle = NanoVG.nvgCreateImageMem(vg, 0, buffer);
        if (handle == -1) {
            throw new IOException("Unable to load image");
        }
        return new Resource(buffer, handle);
    }

    public static InputStream loadWebResource(String path) throws IOException {
        int timeout = 7;
        URL url = new URL(path);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(timeout * 1000);
        connection.setReadTimeout(timeout * 1000);
        return connection.getInputStream();
    }

    public static void runThrowable(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void delete(Path path) throws IOException {
        try (Stream<Path> stream = Files.walk(path)) {
            List<Path> files = stream.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path file : files) {
                System.out.println("Removing " + file.toAbsolutePath());
                Files.deleteIfExists(file);
            }
        }
    }

    @FunctionalInterface
    public interface ThrowableRunnable {

        void run() throws IOException;

    }

    public static class Resource {

        private final ByteBuffer buffer;
        private final int handle;

        public Resource(ByteBuffer buffer, int handle) {
            this.buffer = buffer;
            this.handle = handle;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public int getHandle() {
            return handle;
        }

    }

    public static class Lazy<T> {

        private final Supplier<T> supplier;
        private T cached;

        public Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            if (cached == null) {
                cached = supplier.get();
            }
            return cached;
        }

    }

}
