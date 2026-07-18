package me.deftware.installer.view.components;

import me.deftware.installer.Utils;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.*;

public class CheckBox extends Component<CheckBox> {

    public static final int SIZE = 25;

    private boolean checked;
    private String label;
    private Runnable onChange;

    public CheckBox(int size) {
        super(size, size);
        backgroundColor = Utils.getColor(Color.white);
    }

    public CheckBox() {
        this(SIZE);
    }

    public CheckBox withOnChange(Runnable onChange) {
        this.onChange = onChange;
        return this;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);

        nvgStrokeColor(vg, backgroundColor);
        nvgFillColor(vg, backgroundColor);

        nvgBeginPath(vg);
        nvgRoundedRect(vg, x, y, width, height, 5);

        if (checked) {
            nvgFill(vg);
        } else {
            nvgStroke(vg);
        }

        if (label != null && !label.isEmpty()) {
            TextRenderer.drawText(label, x + width + 5, y + height / 2, 0, 0, false, 18);
        }
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        if (super.mouseClick(mouseX, mouseY, button)) {
            checked = !checked;
            if (onChange != null) {
                onChange.run();
            }
            return true;
        }
        return false;
    }

    public boolean isChecked() {
        return visible && checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
