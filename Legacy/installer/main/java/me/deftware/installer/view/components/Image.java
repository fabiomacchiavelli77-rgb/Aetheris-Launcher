package me.deftware.installer.view.components;

import org.lwjgl.nanovg.NVGPaint;

import static org.lwjgl.nanovg.NanoVG.*;

public class Image extends Component<Image> {

    private final int handle;
    private NVGPaint paint;

    public Image(int size, int handle) {
        this(size, size, handle);
    }

    public Image(int width, int height, int handle) {
        super(width, height);
        this.handle = handle;
    }

    @Override
    public void init() {
        paint = nvgImagePattern(vg, x, y, width, height, 0, handle, 1f, NVGPaint.create());
    }

    @Override
    public void render(int mouseX, int mouseY) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, width, height, 0);
        nvgFillPaint(vg, paint);
        nvgFill(vg);
    }

}
