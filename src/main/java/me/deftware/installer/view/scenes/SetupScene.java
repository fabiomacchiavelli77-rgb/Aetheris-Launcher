package me.deftware.installer.view.scenes;

import me.deftware.installer.model.Configuration;
import me.deftware.installer.model.Manifest;
import me.deftware.installer.view.components.Button;
import me.deftware.installer.view.components.CheckBox;
import me.deftware.installer.view.components.ComboBox;

import java.util.Collections;

public class SetupScene extends Scene {

    @Override
    public void init() {
        components.clear();

        Manifest manifest = Manifest.getInstance();

        CheckBox forgeInstall = new CheckBox();
        forgeInstall.setLabel("Forge");
        forgeInstall.setVisible(manifest.getVersions().get(0).isForgeSupported());

        ComboBox<Manifest.Version> versionBox = new ComboBox<Manifest.Version>(manifest.getVersions(), version -> "Minecraft " + version.getId()) {
            @Override
            protected void onSelect(Manifest.Version item) {
                forgeInstall.setVisible(item.isForgeSupported());
            }
        }.withCenteredPosition(160)
            .withLabel("Minecraft version")
            .add(this);
        ComboBox<Manifest.Launchers> launcherBox = new ComboBox<>(manifest.getLaunchers(), e -> e.getLauncher().getDisplayName())
                .withCenteredPosition(260)
                .withLabel("Launcher")
                .add(this);

        CheckBox cleanInstall = new CheckBox()
                .withPosition(launcherBox.getX() - 30, 340)
                .add(this);

        CheckBox seedCracker = new CheckBox()
                .withPosition(launcherBox.getX() - 30, 380)
                .add(this);
        seedCracker.setLabel("Install SeedCrackerX");

        CheckBox antiDetection = new CheckBox()
                .withPosition(launcherBox.getX() + 230, 380)
                .add(this);
        antiDetection.setLabel("Anti-detection patch");

        forgeInstall
                .withPosition(launcherBox.getX() + 230, 340)
                .withOnChange(() -> {
                    if (forgeInstall.isChecked()) {
                        seedCracker.setChecked(false);
                        seedCracker.setVisible(false);
                    } else {
                        seedCracker.setVisible(true);
                    }
                })
                .add(this);

        new Button("Next") {
            @Override
            protected boolean onClick(int button) {
                Configuration configuration = new Configuration()
                        .withClean(cleanInstall.isChecked())
                        .withForge(forgeInstall.isChecked())
                        .withSeedCracker(seedCracker.isChecked())
                        .withAntiDetection(antiDetection.isChecked());
                sceneManager.setScene(new LocationSetupScene(configuration, versionBox.getSelectedItem(), launcherBox.getSelectedItem().getLauncher()));
                return true;
            }
        }.withCenteredPosition(430).add(this);
        cleanInstall.setLabel("Clean install");
        Collections.reverse(components);
        setTitle("Setup");
        super.init();
    }

}
