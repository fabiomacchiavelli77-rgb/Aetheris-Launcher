package me.deftware.installer.view.components;

import me.deftware.installer.Utils;
import org.lwjgl.nanovg.NVGColor;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgFill;

public class ProgressBar extends Component<ProgressBar> {

    public static final int WIDTH = 300;
    public static final int HEIGHT = 8;

    private static final NVGColor progressBarBg = Utils.setColor(NVGColor.create(), 1, 1, 1, 0.3f);
    private static final NVGColor progressBarFg = Utils.getColor(Color.white);

    private float counter = 0;
    private String label;
    private int slider;

    public ProgressBar(int width) {
        super(width, HEIGHT);
        slider = width / 5;
    }

    public ProgressBar() {
       this(WIDTH);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, width, height, 5);
        nvgFillColor(vg, progressBarBg);
        nvgFill(vg);

        float delta = (float) (Math.sin(counter) + 1) / 2f;
        float offset = (width - slider) * delta;

        nvgBeginPath(vg);
        nvgRoundedRect(vg, x + offset, y, slider, height, 5);
        nvgFillColor(vg, progressBarFg);
        nvgFill(vg);

        if (label != null && !label.isEmpty()) {
            TextRenderer.drawText(label, x + width / 2, (int) (y + height + 18), 0, 0, true, 18);
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSliderWidth(int slider) {
        this.slider = slider;
    }

    @Override
    public void tick() {
        counter += 0.1f;
    }

}
