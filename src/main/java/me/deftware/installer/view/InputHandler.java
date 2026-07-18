package me.deftware.installer.view;

public interface InputHandler {

    default boolean mouseClick(int mouseX, int mouseY, int button) {
        return false;
    }

    default boolean keyPress(int keyCode, int scancode, String name) {
        return false;
    }

    default boolean scroll(double x, double y) {
        return false;
    }

}
