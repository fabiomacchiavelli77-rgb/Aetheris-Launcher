package me.deftware.installer.view.scenes;

import me.deftware.installer.view.ComponentManager;
import me.deftware.installer.view.SceneManager;
import me.deftware.installer.view.Window;
import me.deftware.installer.view.components.TextRenderer;

public class Scene extends ComponentManager {

    protected String title, subtitle;

    protected final Window window = Window.getInstance();
    protected final SceneManager sceneManager = window.getSceneManager();
    protected final long vg = window.getNVG();

    protected final int xCenter = window.getWidth() / 2;
    protected final int yCenter = window.getHeight() / 2;

    @Override
    public void render(int mouseX, int mouseY) {
        if (title != null && !title.isEmpty()) {
            TextRenderer.drawText(title, window.getWidth() / 2, 60, 0, 0, true, TextRenderer.heading);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            TextRenderer.drawText(subtitle, window.getWidth() / 2, 95, 0, 0, true, TextRenderer.subHeading);
        }
        super.render(mouseX, mouseY);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

}
