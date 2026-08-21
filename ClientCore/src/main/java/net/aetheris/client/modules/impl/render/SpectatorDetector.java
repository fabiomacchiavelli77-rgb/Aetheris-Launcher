package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.util.*;

/**
 * SpectatorDetector — monitors the tab list for players in spectator mode.
 * Alerts in chat when someone enters/exits spectator (possible staff watching).
 * Shows a persistent list of current spectators in chat on toggle.
 */
public class SpectatorDetector extends Module {

    /** Tracks players currently known to be in spectator mode. */
    private final Set<String> currentSpectators = new HashSet<>();

    /** Cooldown: only scan every N ticks to avoid spam. */
    private int tickCounter = 0;
    private static final int SCAN_INTERVAL = 20; // every 1 second

    public SpectatorDetector() {
        super("SpectatorDetector", "Detects players in spectator mode (possible staff)", Category.RENDER);
    }

    @Override
    public void onEnable() {
        currentSpectators.clear();
        tickCounter = 0;

        if (mc.player == null || mc.getConnection() == null) return;

        // Initial scan — show who is already in spectator
        Set<String> initial = scanSpectators();
        currentSpectators.addAll(initial);

        mc.player.displayClientMessage(
                Component.literal("§a[Aetheris] §7SpectatorDetector §aON"), false);

        if (initial.isEmpty()) {
            mc.player.displayClientMessage(
                    Component.literal("§7No players in spectator mode detected."), false);
        } else {
            mc.player.displayClientMessage(
                    Component.literal("§e⚠ Spectators online: §f" + String.join(", ", initial)), false);
        }
    }

    @Override
    public void onDisable() {
        currentSpectators.clear();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§7[Aetheris] SpectatorDetector §cOFF"), false);
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getConnection() == null) return;

        tickCounter++;
        if (tickCounter < SCAN_INTERVAL) return;
        tickCounter = 0;

        Set<String> nowSpectators = scanSpectators();

        // Detect new spectators (entered spectator mode)
        for (String name : nowSpectators) {
            if (!currentSpectators.contains(name)) {
                mc.player.displayClientMessage(
                        Component.literal("§e⚠ [SpectatorDetector] §c" + name + " §eentered spectator mode!"), false);
            }
        }

        // Detect players who left spectator mode
        for (String name : currentSpectators) {
            if (!nowSpectators.contains(name)) {
                mc.player.displayClientMessage(
                        Component.literal("§a✓ [SpectatorDetector] §f" + name + " §aleft spectator mode."), false);
            }
        }

        // Update tracked set
        currentSpectators.clear();
        currentSpectators.addAll(nowSpectators);
    }

    /**
     * Scans the tab list for players with GameType.SPECTATOR.
     * Excludes the local player.
     */
    private Set<String> scanSpectators() {
        Set<String> spectators = new LinkedHashSet<>();
        if (mc.getConnection() == null) return spectators;

        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
        if (players == null) return spectators;

        for (PlayerInfo info : players) {
            if (info == null) continue;
            if (info.getGameMode() == GameType.SPECTATOR) {
                if (info.getProfile() == null || info.getProfile().name() == null) continue;
                String name = info.getProfile().name();
                // Exclude self
                if (mc.player != null && mc.player.getGameProfile() != null && name.equals(mc.player.getGameProfile().name())) continue;
                spectators.add(name);
            }
        }
        return spectators;
    }
}
