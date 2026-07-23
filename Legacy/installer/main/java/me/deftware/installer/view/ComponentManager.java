package me.deftware.installer.view;

import me.deftware.installer.view.components.ComboBox;
import me.deftware.installer.view.components.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ComponentManager implements EventHandler, InputHandler {

    protected final List<Component<?>> components = new CopyOnWriteArrayList<>();

    public void add(Component<?> component) {
        this.components.add(component);
    }

    public void remove(Component<?> component) {
        this.components.remove(component);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        for (Component<?> component : components) {
            if (component.isVisible()) {
                component.render(mouseX, mouseY);
            }
        }
    }

    @Override
    public void init() {
        for (Component<?> component : components) {
            component.init();
        }
    }

    @Override
    public void tick() {
        for (Component<?> component : components) {
            component.tick();
        }
    }

    @Override
    public boolean mouseClick(int mouseX, int mouseY, int button) {
        for (int i = components.size() - 1; i >= 0; i--) {
            Component<?> component = components.get(i);
            if (component.isVisible() && component.mouseClick(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPress(int keyCode, int scancode, String name) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).keyPress(keyCode, scancode, name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean scroll(double x, double y) {
        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).scroll(x, y)) {
                return true;
            }
        }
        return false;
    }

}
