package me.deftware.installer.view.components;

import me.deftware.installer.view.InputHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.nanovg.NanoVG.*;

public abstract class ListComponent<T extends InputHandler> extends Component<ListComponent<T>> {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 250;

    private final List<T> items = new CopyOnWriteArrayList<>();
    protected int itemHeight;
    private int gap = 10;
    private T selected;

    public ListComponent(int itemHeight) {
        super(WIDTH, HEIGHT);
        this.itemHeight = itemHeight;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);
        nvgSave(vg);
        nvgScissor(vg, x - 2, y - 2, width + 2, height + 2);
        for (int i = 0; i < items.size(); i++) {
            int offset = i * (gap + itemHeight);
            T item = items.get(i);

            // Background
            nvgBeginPath(vg);
            nvgStrokeColor(vg, TextRenderer.textColor);
            nvgFillColor(vg, backgroundColor);
            nvgRoundedRect(vg, x, y + offset, width, itemHeight, 5);
            nvgFill(vg);
            nvgStroke(vg);

            // Selection
            nvgFillColor(vg, TextRenderer.textColor);
            nvgStrokeColor(vg, TextRenderer.textColor);

            int radius = 15;
            nvgBeginPath(vg);
            nvgCircle(vg, x + width - radius * 2 - 5, y + offset + itemHeight / 2f, radius + 3);
            nvgStroke(vg);

            if (selected == item) {
                nvgBeginPath(vg);
                nvgCircle(vg, x + width - radius * 2 - 5, y + offset + itemHeight / 2f, radius);
                nvgFill(vg);
            }

            render(item, y + offset);
        }
        nvgResetScissor(vg);
        nvgRestore(vg);
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        if (super.mouseClick(mouseX, mouseY, button)) {
            int index = (mouseY - y) / (itemHeight + gap);
            if (index < items.size()) {
                T item = items.get(index);
                if (!item.mouseClick(mouseX, mouseY, button)) {
                    selected = items.get(index);
                }
                return true;
            }
        }
        return false;
    }

    public void add(T item) {
        if (items.isEmpty()) {
            selected = item;
        }
        items.add(item);
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T item) {
        selected = item;
    }

    protected abstract void render(T item, int y);

}
