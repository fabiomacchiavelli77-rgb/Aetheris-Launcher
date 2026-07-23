package me.deftware.installer.model;

public class Configuration {

    private boolean clean;
    private boolean forge;
    private boolean seedCracker;
    private boolean antiDetection;
    private String instance;

    public Configuration withClean(boolean state) {
        clean = state;
        return this;
    }

    public Configuration withForge(boolean state) {
        forge = state;
        return this;
    }

    public Configuration withSeedCracker(boolean state) {
        seedCracker = state;
        return this;
    }

    public Configuration withAntiDetection(boolean state) {
        antiDetection = state;
        return this;
    }

    public Configuration withInstance(String instance) {
        this.instance = instance;
        return this;
    }

    public boolean isClean() {
        return clean;
    }

    public boolean isForge() {
        return forge;
    }

    public boolean isSeedCracker() {
        return seedCracker;
    }

    public boolean isAntiDetection() {
        return antiDetection;
    }

    public String getInstance() {
        return instance;
    }
}
