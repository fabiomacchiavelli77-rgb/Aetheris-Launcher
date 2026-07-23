package me.deftware.installer.view;

import me.deftware.installer.view.scenes.Scene;

import static org.lwjgl.glfw.GLFW.*;

public class SceneManager{

    protected final Window window;

    private Scene scene;
    private int cursorX;
    private int cursorY;

    public SceneManager(Window window) {
        this.window = window;
        this.events(window.getHandle());
    }

    private void events(long window) {
        glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> {
            if (action == GLFW_RELEASE) {
                scene.mouseClick(cursorX, cursorY, button);
            }
        });
        glfwSetCursorPosCallback(window, (handle, xpos, ypos) -> {
            cursorX = (int) xpos;
            cursorY = (int) ypos;
        });
        glfwSetKeyCallback(window, (windowHandle, keyCode, scancode, action, mods) -> {
            if (action == GLFW_RELEASE) {
                String name = glfwGetKeyName(keyCode, scancode);
                scene.keyPress(keyCode, scancode, name);
            }
        });
        glfwSetScrollCallback(window, (handle, x, y) -> {
            scene.scroll(x, y);
        });
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        scene.init();
        this.scene = scene;
    }

    public void render() {
        scene.render(cursorX, cursorY);
    }

    public void tick() {
        scene.tick();
    }

}
