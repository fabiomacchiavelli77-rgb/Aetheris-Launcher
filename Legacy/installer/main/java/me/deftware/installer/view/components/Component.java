package me.deftware.installer.view.components;

import me.deftware.installer.view.EventHandler;
import me.deftware.installer.view.InputHandler;
import me.deftware.installer.Utils;
import me.deftware.installer.view.Window;
import me.deftware.installer.view.scenes.Scene;
import org.lwjgl.nanovg.NVGColor;

import java.awt.Color;

import static org.lwjgl.nanovg.NanoVG.*;

@SuppressWarnings("unchecked")
public abstract class Component<T extends Component<T>> implements EventHandler, InputHandler {

    protected final Window window = Window.getInstance();
    protected final long vg = window.getNVG();

    public static final Color background = new Color(36, 58, 82);
    protected NVGColor backgroundColor = Utils.getColor(background);

    public static final int DefaultHeight = 50;

    protected boolean visible = true;
    protected int x, y, width, height;
    protected boolean mouseOver;
    private String label;

    public Component(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        mouseOver = isWithinBounds(mouseX, mouseY);
        if (label != null && !label.isEmpty()) {
            TextRenderer.drawText(label, x, y - TextRenderer.small, 0, 0, false, TextRenderer.small);
        }
    }

    public T withPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return (T) this;
    }

    public T withCenteredPosition(int x, int y) {
        this.x = x - width / 2;
        this.y = y;
        return (T) this;
    }

    public T withCenteredPosition(int y) {
        this.x = window.getWidth() / 2 - width / 2;
        this.y = y;
        return (T) this;
    }

    public T withLabel(String label) {
        this.label = label;
        return (T) this;
    }

    public T add(Scene scene) {
        scene.add(this);
        return (T) this;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        return mouseOver;
    }

    public boolean isWithinBounds(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    protected void drawBackground(long vg, int rounding) {
        nvgBeginPath(vg);
        nvgFillColor(vg, backgroundColor);
        nvgRoundedRect(vg, x, y, width, height, rounding);
        nvgFill(vg);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}
