package me.deftware.installer.model.process;

import java.io.IOException;

public interface LauncherProcess {

    boolean isRunning() throws IOException;

    void start() throws IOException;

    void stop() throws IOException;

}
