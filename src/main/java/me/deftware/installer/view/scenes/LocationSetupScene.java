package me.deftware.installer.view.scenes;

import me.deftware.installer.model.Config;
import me.deftware.installer.model.Configuration;
import me.deftware.installer.model.Manifest;
import me.deftware.installer.model.launcher.Launcher;
import me.deftware.installer.model.launcher.PrismLauncher;
import me.deftware.installer.model.provider.HttpProvider;
import me.deftware.installer.model.provider.Provider;
import me.deftware.installer.view.InputHandler;
import me.deftware.installer.view.components.Button;
import me.deftware.installer.view.components.ListComponent;
import me.deftware.installer.view.components.TextRenderer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocationSetupScene extends Scene {

    private final Configuration configuration;
    private final Manifest.Version version;
    private final Launcher launcher;

    public LocationSetupScene(Configuration configuration, Manifest.Version version, Launcher launcher) {
        this.configuration = configuration;
        this.version = version;
        this.launcher = launcher;
    }

    @Override
    public void init() {
        components.clear();

        ListComponent<DirectoryItem> list = new ListComponent<DirectoryItem>(70) {
            @Override
            protected void render(DirectoryItem item, int y) {
                Path path = item.path;
                if (path == null) {
                    TextRenderer.drawText("Choose directory...", x + 15, y, width, itemHeight, false);
                    return;
                }
                int names = path.getNameCount();
                Path subpath = path;
                if (names > 1) {
                    subpath = path.subpath(0, names - 1);
                }
                TextRenderer.drawText(path.getFileName().toString(), x + 10, y + 12, 0, itemHeight, false);
                TextRenderer.drawText(subpath.toString(), x + 10, y + 20, 0, 0, false, TextRenderer.small);
            }
        }.withCenteredPosition(145).add(this);

        List<Path> paths = launcher.getCommonPaths();
        for (Path path : paths) {
            if (Files.exists(path)) {
                list.add(new DirectoryItem(path));
            }
        }

        list.add(new DirectoryItem(null) {
            @Override
            public boolean mouseClick(int mouseX, int mouseY, int button) {
                if (this.path == null) {
                    String folder = TinyFileDialogs.tinyfd_selectFolderDialog("Select launcher directory", ".");
                    if (folder == null || folder.isEmpty()) {
                        return true;
                    }
                    Path path = Paths.get(folder);
                    if (!launcher.isValidPath(path)) {
                        if (!TinyFileDialogs.tinyfd_messageBox("Missing install location",
                                "The specified directory does not appear to be\na valid launcher directory.\n\nDo you want to use it anyway?",
                                "yesno", "warning", true)) {
                            return false;
                        }
                    }
                    this.path = path;
                    list.setSelected(this);
                    return true;
                }
                return false;
            }
        });

        new Button("Install") {
            @Override
            protected boolean onClick(int button) {
                if (list.getSelected() == null || list.getSelected().path == null) {
                    TinyFileDialogs.tinyfd_messageBox("Missing install location", "Please select a valid install location",
                            "ok", "error", true);
                    return false;
                }
                Path root = list.getSelected().path;
                Provider provider = new HttpProvider(version)
                        .withLauncher(launcher.getId())
                        .withDonor(Config.getInstance().isDonor());
                if (configuration.isForge() && launcher instanceof PrismLauncher) {
                    try {
                        List<String> instances = ((PrismLauncher) launcher).getInstances(root, provider, "net.minecraftforge");
                        if (instances.isEmpty()) {
                            TinyFileDialogs.tinyfd_messageBox("Missing Forge instance", "You must have at least one Forge instance\nfor the selected Minecraft version.\n\nPlease create one and then press install again.",
                                    "ok", "error", true);
                        } else {
                            sceneManager.setScene(new ForgeInstanceScene(root, provider, instances));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
                sceneManager.setScene(new InstallingScene(configuration, provider, launcher, root));
                return true;
            }
        }.withCenteredPosition(410).add(this);

        setSubtitle("We found the following launcher directories");
        setTitle("Select " + launcher.getDisplayName() + " directory");
        super.init();
    }

    private class ForgeInstanceScene extends Scene {

        private final Path root;
        private final Provider provider;
        private final List<String> instances;

        public ForgeInstanceScene(Path root, Provider provider, List<String> instances) {
            this.root = root;
            this.provider = provider;
            this.instances = instances;
        }

        @Override
        public void init() {

            ListComponent<InstanceItem> list = new ListComponent<InstanceItem>(60) {
                @Override
                protected void render(InstanceItem item, int y) {
                    TextRenderer.drawText(item.instance, x + 15, y, width, itemHeight, false);
                }
            }.withCenteredPosition(145).add(this);

            for (String instance : instances) {
                list.add(new InstanceItem(instance));
            }

            new Button("Install") {
                @Override
                protected boolean onClick(int button) {
                    configuration.withInstance(list.getSelected().getInstance());
                    sceneManager.setScene(new InstallingScene(configuration, provider, launcher, root));
                    return true;
                }
            }.withCenteredPosition(410).add(this);

            setTitle("Which Forge instance?");
            setSubtitle("Select Forge instance to install Aristois in");
            super.init();
        }

    }

    private static class DirectoryItem implements InputHandler {

        protected Path path;

        public DirectoryItem(Path path) {
            this.path = path;
        }

    }

    private static class InstanceItem implements InputHandler {

        private final String instance;

        public InstanceItem(String instance) {
            this.instance = instance;
        }

        public String getInstance() {
            return instance;
        }

    }

}
