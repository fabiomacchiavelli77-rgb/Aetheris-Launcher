package me.deftware.installer.view;

public interface EventHandler {

    void render(int mouseX, int mouseY);

    default void init() {
        //
    }

    default void tick() {
        //
    }

}
