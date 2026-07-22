package net.aetheris.client.modules;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    PLAYER("Player"),
    SEEDCRACKER("SeedCracker");

    private final String name;

    Category(String name) { this.name = name; }
    public String getName() { return name; }
}
