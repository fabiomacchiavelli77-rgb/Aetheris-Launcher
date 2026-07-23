package me.deftware.installer;

import me.deftware.installer.view.Window;
import me.deftware.installer.view.scenes.WelcomeScene;
import org.lwjgl.system.Platform;

public class Main {

    public static void main(String[] args) {
        if (Platform.get() == Platform.MACOSX) {
            AppleHelper helper = new AppleHelper();
            if (!helper.isOnFirstThread()) {
                helper.restart();
                return;
            }
        }
        if (System.getProperty("java.version").startsWith("1.8")) {
            try {
                SSLUtil.init();
            } catch (Exception ex) {
                System.err.println("Unable to load certificates");
                ex.printStackTrace();
            }
        }
        Window window = new Window();
        window.getSceneManager().setScene(new WelcomeScene());
        window.loop();
    }

}
