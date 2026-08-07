package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.SliderSetting;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerFinder extends Module {

    private final SliderSetting threads = new SliderSetting("threads", "Threads", "Thread simultanei", 50.0, 10.0, 200.0, 10.0);
    private final SliderSetting timeout = new SliderSetting("timeout", "Timeout (ms)", "Timeout connessione (ms)", 1000.0, 200.0, 5000.0, 100.0, "ms");
    private final SliderSetting maxRange = new SliderSetting("maxRange", "IP Range", "Numero di IP da verificare", 255.0, 10.0, 1000.0, 10.0);

    private ExecutorService executor;
    private AtomicInteger foundServers = new AtomicInteger(0);
    private AtomicInteger scannedCount = new AtomicInteger(0);

    public ServerFinder() {
        super("ServerFinder", "Scansiona un intervallo di IP per individuare server Minecraft attivi.", Category.WORLD);
        addSetting(threads);
        addSetting(timeout);
        addSetting(maxRange);
    }

    @Override
    public void onEnable() {
        if (mc.getCurrentServer() == null && mc.player == null) {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§c[ServerFinder] Collegati prima a un server di riferimento per definire la subnet IP!"), false);
            }
            setEnabled(false);
            return;
        }

        String baseIp = "127.0.0.1";
        if (mc.getCurrentServer() != null) {
            baseIp = mc.getCurrentServer().ip;
        }

        String subnet = getSubnet(baseIp);
        if (subnet == null) {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§c[ServerFinder] IP non valido: " + baseIp), false);
            }
            setEnabled(false);
            return;
        }

        int threadCount = (int) Math.round(threads.getValue());
        int timeoutMs = (int) Math.round(timeout.getValue());
        int rangeLimit = (int) Math.round(maxRange.getValue());

        executor = Executors.newFixedThreadPool(threadCount);
        foundServers.set(0);
        scannedCount.set(0);

        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§a[ServerFinder] §7Avvio scansione su subnet §e" + subnet + ".x§7 (limite: " + rangeLimit + " IP)..."), false);
        }

        for (int i = 1; i <= Math.min(255, rangeLimit); i++) {
            final String targetIp = subnet + "." + i;
            executor.submit(() -> scanIp(targetIp, 25565, timeoutMs));
        }
    }

    private void scanIp(String ip, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeoutMs);
            foundServers.incrementAndGet();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§a[ServerFinder] §fTrovato server attivo: §e" + ip + ":" + port), false);
            }
        } catch (Exception ignored) {
        } finally {
            scannedCount.incrementAndGet();
        }
    }

    @Override
    public void onDisable() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§6[ServerFinder] §7Scansione terminata. Server trovati: §a" + foundServers.get()), false);
        }
    }

    private String getSubnet(String ip) {
        String host = ip.split(":")[0];
        String[] parts = host.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2];
        }
        return null;
    }
}
