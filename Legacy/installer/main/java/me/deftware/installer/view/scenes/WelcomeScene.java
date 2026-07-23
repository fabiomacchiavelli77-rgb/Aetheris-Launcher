package me.deftware.installer.view.scenes;

import me.deftware.installer.model.Config;
import me.deftware.installer.model.Manifest;
import me.deftware.installer.model.launcher.PistonMeta;
import me.deftware.installer.view.Window;
import me.deftware.installer.view.components.Button;
import me.deftware.installer.view.components.Image;
import me.deftware.installer.view.components.ProgressBar;
import me.deftware.installer.view.components.TextRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class WelcomeScene extends Scene {

    private final static Logger logger = LoggerFactory.getLogger("Loader");

    private void fetch() throws Exception {
        Thread.currentThread().setName("Init thread");
        logger.info("Fetching available versions...");
        try {
            progressBar.setLabel("Fetching versions");
            Manifest manifest = Manifest.init();
            logger.info("Found {} available versions", manifest.getVersions().size());
        } catch (Exception ex) {
            throw new Exception("Unable to fetch available versions");
        }

        try {
            progressBar.setLabel("Fetching metadata");
            PistonMeta.init();
        } catch (Exception ex) {
            logger.error("Unable to load Minecraft metadata", ex);
        }

        /* TODO: Replace with Aristois API
        progressBar.setLabel("Checking for updates");
        try (InputStream stream = Utils.loadWebResource("https://gitlab.com/Aristois/dev-installer/-/raw/master/src/main/resources/config.json");
             Reader reader = new InputStreamReader(stream)) {
            Config config = Utils.GSON.fromJson(reader, Config.class);
            if (!Config.getInstance().getVersion().equals(config.getVersion())) {
                window.runOnRenderThread(() -> sceneManager.setScene(new UpdateScene(config)));
            }
        } catch (Exception ex) {
            logger.error("Unable to check for updates", ex);
        }
         */
    }

    private ProgressBar progressBar;

    @Override
    public void init() {
        components.clear();

        Image logo = new Image(400, Window.getInstance().getLogo().getHandle())
                .withCenteredPosition(0)
                .add(this);

        progressBar = new ProgressBar()
                .withCenteredPosition(logo.getHeight() - 20)
                .add(this);
        progressBar.setLabel("Waiting");

        Button button = new Button("Continue") {
            @Override
            protected boolean onClick(int button) {
                sceneManager.setScene(new SetupScene());
                return true;
            }
        }.withCenteredPosition(logo.getHeight() - 20);

        CompletableFuture.runAsync(() -> {
            try {
                fetch();
                remove(progressBar);
                add(button);
            } catch (Exception ex) {
                progressBar.setLabel(ex.getMessage());
            }
        });

        super.init();
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);

        Config config = Config.getInstance();
        TextRenderer.drawText(
                 config.getHash(),
                5, window.getHeight() - 15, 0, 0, false, 15
        );
        TextRenderer.drawText(
                config.getVersion(),
                5, 15, 0, 0, false, 15
        );
    }

}
