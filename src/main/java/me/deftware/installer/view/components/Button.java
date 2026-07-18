package me.deftware.installer.view.components;

import me.deftware.installer.Utils;

public abstract class Button extends Component<Button> {

    public static final int DefaultHeight = 50;
    public static final int DefaultWidth = 200;

    private String text;

    public Button(String text) {
        this(DefaultWidth, text);
    }

    public Button(int width, String text) {
        super(width, DefaultHeight);
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);
        drawBackground(vg, 25);
        TextRenderer.drawText(text, x, y, width, height, true);
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        if (super.mouseClick(mouseX, mouseY, button)) {
            return this.onClick(button);
        }
        return false;
    }

    protected abstract boolean onClick(int button);

    public void setText(String text) {
        this.text = text;
    }

    private float ratio = 0.5f;
    private final float delta = 0.02f;

    @Override
    public void tick() {
        if (mouseOver) {
            if (ratio > 0.4f) {
                ratio -= delta;
            }
        } else if (ratio < 0.5f) {
            ratio += delta;
        }
        Utils.blend(backgroundColor, ratio, background, background.brighter());
    }

}
