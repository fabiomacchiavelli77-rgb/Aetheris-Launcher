package me.deftware.installer;

import org.lwjgl.system.macosx.LibC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Allows lwjgl to start on macOS without manually
 * specifying -XstartOnFirstThread in the terminal.
 *
 * @author Deftware
 */
public class AppleHelper {

    private static final List<String> jvmFlags = Collections.singletonList("-XstartOnFirstThread");

    private final Logger logger = LoggerFactory.getLogger("AppleHelper");

    public boolean isOnFirstThread() {
        long pid = LibC.getpid();
        String startedOnThread = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);
        return startedOnThread != null && startedOnThread.equals("1");
    }

    public void restart() {
        logger.info("Requested restart with JVM flags {}", jvmFlags);

        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path binary = javaHome.resolve("bin").resolve("java");
        if (!Files.exists(binary)) {
            throw new RuntimeException("Unable to locate Java binary");
        }

        Path jarFile = Paths.get(System.getProperty("java.class.path"));
        if (!Files.exists(jarFile)) {
            throw new RuntimeException("Unable to locate jar file");
        }

        logger.info("Using Java binary {}", binary);
        logger.info("Using jar file {}", jarFile);

        List<String> args = new ArrayList<>();
        args.add(binary.toAbsolutePath().toString());
        args.addAll(jvmFlags);
        args.add("-jar");
        args.add(jarFile.toAbsolutePath().toString());

        runProcess(args);
    }

    private void runProcess(List<String> args) {
        try {
            logger.info("Starting process...");
            Process process = new ProcessBuilder(args)
                    .redirectErrorStream(true)
                    .start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int status = process.waitFor();
            logger.info("Process exited with status code {}", status);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
