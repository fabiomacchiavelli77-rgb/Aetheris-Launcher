package me.deftware.installer.view.scenes;

import me.deftware.installer.model.Config;
import me.deftware.installer.view.components.Button;
import me.deftware.installer.view.components.TextRenderer;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UpdateScene extends Scene {

    private final Config remote;
    private final List<String> lines = new ArrayList<>();

    public UpdateScene(Config remote) {
        this.remote = remote;
    }

    @Override
    public void init() {
        int width = Button.DefaultWidth, gap = 5;
        int y = 400;
        new Button("Close") {
            @Override
            protected boolean onClick(int button) {
                GLFW.glfwSetWindowShouldClose(window.getHandle(), true);
                return true;
            }
        }.withPosition(xCenter - width - gap, y).add(this);
        new Button("Download") {
            @Override
            protected boolean onClick(int button) {
                try {
                    Desktop.getDesktop().browse(new URL("https://aristois.net/download").toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            }
        }.withPosition(xCenter + gap, y).add(this);
        setTitle("Update available");
        setSubtitle("A new version of the installer is available");
        lines.add("Current installer version is " + Config.getInstance().getVersion());
        lines.add("New version available is " + remote.getVersion());
        lines.add("Not updating may cause the installer not to work");
        super.init();
    }

    @Override
    public boolean keyPress(int keyCode, int scancode, String name) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            sceneManager.setScene(new SetupScene());
            return true;
        }
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        int fontSize = 22;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            TextRenderer.drawText(line, xCenter, window.getHeight() / 2 - 60 + fontSize * i, 0, 0, true, fontSize);
        }
        super.render(mouseX, mouseY);
    }

}
