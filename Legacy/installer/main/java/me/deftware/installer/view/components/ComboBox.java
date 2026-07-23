package me.deftware.installer.view.components;

import java.util.List;
import java.util.function.Function;

import static org.lwjgl.nanovg.NanoVG.*;

public class ComboBox<T> extends Component<ComboBox<T>> {

    public static final int WIDTH = 600;
    private static final int numItems = 5;

    private boolean expanded = false;
    private final int itemHeight;

    private final List<T> items;
    private final Function<T, String> labelSupplier;
    private int selectedIndex = 0, scroll = 0;

    public ComboBox(int width, List<T> items, Function<T, String> labelSupplier) {
        super(width, DefaultHeight);
        itemHeight = height;
        this.items = items;
        this.labelSupplier = labelSupplier;
    }

    public ComboBox(List<T> items, Function<T, String> labelSupplier) {
        this(WIDTH, items, labelSupplier);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);
        nvgBeginPath(vg);
        nvgStrokeColor(vg, TextRenderer.textColor);
        nvgRoundedRect(vg, x, y, width, height, 5);
        if (expanded) {
            nvgMoveTo(vg, x, y + itemHeight);
            nvgLineTo(vg, x + width, y + itemHeight);
        }
        nvgStroke(vg);

        if (expanded) {
            nvgBeginPath(vg);
            nvgFillColor(vg, backgroundColor);
            nvgRoundedRect(vg, x, y + itemHeight, width, height - itemHeight, 5);
            nvgFill(vg);

            if (items.size() > numItems) {
                float scrollbarWidth = 5;

                float steps = items.size() - numItems;
                float viewport = numItems * itemHeight;

                float scrollbarHeight = 60;
                float delta = (viewport - scrollbarHeight) / steps;

                nvgBeginPath(vg);
                nvgFillColor(vg, TextRenderer.textColor);
                nvgRoundedRect(vg, x + width - scrollbarWidth, y + itemHeight + delta * scroll, scrollbarWidth, scrollbarHeight, 2);
                nvgFill(vg);
            }
        }

        drawItem(vg, items.get(selectedIndex), y);
        if (expanded) {
            for (int i = 0; i < getNumItems(); i++) {
                drawItem(vg, items.get(scroll + i), y + itemHeight * (i + 1));
            }
        }
    }

    private void drawItem(long vg, T item, int y) {
        String label = labelSupplier.apply(item);
        TextRenderer.drawText(label, x + 5, y, width, itemHeight, false);
    }

    @Override
    public boolean scroll(double x, double y) {
        if (mouseOver && items.size() > numItems) {
            int delta = (int) y * -1;
            scroll = clamp(0, items.size() - numItems, scroll + delta);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        height = itemHeight;
        if (super.mouseClick(mouseX, mouseY, button)) {
            if (mouseY > y + itemHeight) {
                selectedIndex = (mouseY - y) / itemHeight - 1 + scroll;
                onSelect(getSelectedItem());
            }
            expanded = !expanded;
            if (expanded) {
                height *= getNumItems() + 1;
            }
            return true;
        }
        expanded = false;
        return false;
    }

    public T getSelectedItem() {
        return items.get(selectedIndex);
    }

    private int getNumItems() {
        return Math.min(numItems, items.size());
    }

    private int clamp(int min, int max, int value) {
        if (value < min)
            return min;
        return Math.min(value, max);
    }

    protected void onSelect(T item) { }

}
