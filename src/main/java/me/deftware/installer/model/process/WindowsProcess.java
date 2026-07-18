package me.deftware.installer.model.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowsProcess implements LauncherProcess {

    private final String binary;
    private final Path path = Paths.get("C:\\Program Files (x86)\\Minecraft Launcher");

    public WindowsProcess(String binary) {
        this.binary = binary;
    }

    @Override
    public boolean isRunning() throws IOException {
        return isProcessRunning(binary);
    }

    @Override
    public void start() throws IOException {
        Path executable = path.resolve(binary);
        if (Files.exists(executable)) {
            Runtime.getRuntime().exec(executable.toAbsolutePath().toString());
        }
    }

    @Override
    public void stop() throws IOException {
        Runtime.getRuntime().exec("taskkill /F /IM " + binary);
    }

    public static boolean isProcessRunning(String name) throws IOException {
        Process p = Runtime.getRuntime().exec("tasklist");
        try (
                InputStream stream = p.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(reader)
        ) {
            return bufferedReader.lines().anyMatch(l -> l.contains(name));
        }
    }

}
