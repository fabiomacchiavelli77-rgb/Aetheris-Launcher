package me.deftware.installer.view;

import me.deftware.installer.Main;
import me.deftware.installer.Utils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Platform;

import java.awt.*;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Stack;

import static org.lwjgl.stb.STBImage.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    private static Window instance;

    public static final Color backgroundColor = new Color(19, 29, 39);
    private static final double deltaTime = 1000d / 60;

    public int framebufferWidth = 800;
    public int framebufferHeight = 500;

    public final long handle, vg;

    private long lastFrame = System.currentTimeMillis();
    private long accumulator = 0;

    private final SceneManager sceneManager;
    private final Stack<Runnable> actions = new Stack<>();

    public Window() {
        Thread.currentThread().setName("Render Thread");
        if (!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW.");
        }
        setHints();
        handle = glfwCreateWindow(framebufferWidth, framebufferHeight, "Aristois Installer", NULL, NULL);
        if (handle == NULL) {
            glfwTerminate();
            throw new RuntimeException();
        }
        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(1); // Enable vsync
        vg = nvgCreate(NVG_ANTIALIAS);
        if (vg == NULL) {
            glfwTerminate();
            throw new RuntimeException("Could not init nanovg.");
        }
        loadResources();
        if (Platform.get() == Platform.WINDOWS) {
            setIcon();
        }
        instance = this;
        sceneManager = new SceneManager(this);
    }

    private Utils.Resource font;
    private Utils.Resource logo;

    private void loadResources() {
        try {
            // Fonts
            try (InputStream stream = Main.class.getResourceAsStream("/Roboto-Regular.ttf")) {
                font = Utils.loadFont(vg, Utils.defaultFont, stream);
            }
            // Images
            try (InputStream stream = Main.class.getResourceAsStream("/logo.png")) {
                logo = Utils.loadImage(vg, stream);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to load resources", ex);
        }
    }

    private void setIcon() {
        try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
            IntBuffer w = memAllocInt(1);
            IntBuffer h = memAllocInt(1);
            IntBuffer comp = memAllocInt(1);

            ByteBuffer pixels = stbi_load_from_memory(logo.getBuffer(), w, h, comp, 4);
            icons.position(0)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels);

            glfwSetWindowIcon(handle, icons);

            memFree(w);
            memFree(h);
            memFree(comp);

            stbi_image_free(pixels);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setHints() {
        if (Platform.get() == Platform.MACOSX) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
        }
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, 8);
    }

    private void tick(Runnable onTick) {
        long now = System.currentTimeMillis();
        long passed = now - lastFrame;
        lastFrame = now;
        accumulator += passed;
        while (accumulator >= deltaTime) {
            onTick.run();
            accumulator-= deltaTime;
        }
    }

    public void loop() {
        while (!glfwWindowShouldClose(handle)) {
            // Update and render
            glViewport(0, 0, framebufferWidth, framebufferHeight);
            glClearColor(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            tick(sceneManager::tick);

            nvgBeginFrame(vg, framebufferWidth, framebufferHeight, 1);
            sceneManager.render();
            nvgEndFrame(vg);

            glfwSwapBuffers(handle);
            glfwPollEvents();

            if (!actions.isEmpty()) {
                actions.pop().run();
            }
        }

        GL.setCapabilities(null);
        nvgDelete(vg);
        glfwFreeCallbacks(handle);
        glfwTerminate();
    }

    public void runOnRenderThread(Runnable runnable) {
        actions.push(runnable);
    }

    public static Window getInstance() {
        return instance;
    }

    public long getHandle() {
        return handle;
    }

    public long getNVG() {
        return vg;
    }

    public int getWidth() {
        return framebufferWidth;
    }

    public int getHeight() {
        return framebufferHeight;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public Utils.Resource getFont() {
        return font;
    }

    public Utils.Resource getLogo() {
        return logo;
    }

}
