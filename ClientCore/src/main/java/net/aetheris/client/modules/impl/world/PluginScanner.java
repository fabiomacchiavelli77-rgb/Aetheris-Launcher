package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PluginScanner - enumera i plugin del server tramite /plugins e i command dispatcher,
 * identifica i plugin di permessi (PEX, LuckPerms, GroupManager, Vault...) e
 * stampa i comandi di probe utilizzabili per verificare permessi/esposizione.
 *
 * Non invia exploit: solo scan + report. I plugin anti-hack non possono
 * distinguere questo modulo da un utente che digita /plugins a mano.
 */
public class PluginScanner extends Module {

    private static PluginScanner instance;

    private final BooleanSetting autoProbe = new BooleanSetting("autoProbe", "Auto Probe", "Testa comandi di permesso via /pex e /lp", true);
    private final BooleanSetting showSuggestions = new BooleanSetting("suggestions", "Show Suggestions", "Mostra comandi di test scoperti", true);

    /** Plugin permessi noti e relativo comando di probing. */
    private static final Map<String, String[]> PERMISSION_PLUGINS = new LinkedHashMap<>();
    static {
        PERMISSION_PLUGINS.put("permissionsex", new String[]{"pex", "pex user", "pex user $name perm list"});
        PERMISSION_PLUGINS.put("pex", new String[]{"pex", "pex user", "pex user $name perm list"});
        PERMISSION_PLUGINS.put("luckperms", new String[]{"lp", "lp user", "lp user $name info"});
        PERMISSION_PLUGINS.put("luckperm", new String[]{"lp", "lp user", "lp user $name info"});
        PERMISSION_PLUGINS.put("groupmanager", new String[]{"manuadd", "manucheckp", "manwhois"});
        PERMISSION_PLUGINS.put("permissionsbukkit", new String[]{"permissions", "permissions user", "permissions user $name info"});
        PERMISSION_PLUGINS.put("vault", new String[]{"vault", "vault info"});
        PERMISSION_PLUGINS.put("essentials", new String[]{"essentials", "essentials version"});
        PERMISSION_PLUGINS.put("essentialsx", new String[]{"essentials", "essentials version"});
        PERMISSION_PLUGINS.put("zpermissions", new String[]{"zp", "zp user"});
        PERMISSION_PLUGINS.put("bpermissions", new String[]{"bp", "bp user"});
    }

    private static final Pattern PLUGIN_LIST = Pattern.compile("[A-Za-z0-9_\\-]+");

    private final List<String> foundPlugins = new ArrayList<>();
    private boolean waitingForResponse = false;
    private int tickCounter = 0;

    public PluginScanner() {
        super("PluginScanner", "Scansiona i plugin del server e identifica plugin di permessi (PEX, LuckPerms...).", Category.WORLD);
        addSetting(autoProbe);
        addSetting(showSuggestions);
        instance = this;
    }

    public static PluginScanner getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        foundPlugins.clear();
        waitingForResponse = true;
        tickCounter = 0;
        if (mc.getConnection() != null) {
            try {
                mc.getConnection().sendCommand("plugins");
            } catch (Exception ignored) {}
        }
        displayMessage("§8Probe /plugins inviato, elaborazione risposta...");
    }

    @Override
    public void onTick() {
        if (!waitingForResponse) return;
        tickCounter++;
        // Timeout 3 secondi: il server potrebbe nascondere /plugins
        if (tickCounter > 60 && !foundPlugins.isEmpty()) {
            waitingForResponse = false;
        }
    }

    /** Chiamato dal mixin quando il server invia un messaggio di sistema. */
    public void onSystemChat(Component message) {
        if (!isEnabled()) return;
        String text = message.getString();
        if (text == null || text.isEmpty()) return;

        String lower = text.toLowerCase(Locale.ROOT);

        // Risposta a /plugins: "Plugins (3): WorldEdit, Essentials, LuckPerms"
        if (lower.contains("plugins") && (lower.contains("(") || lower.contains(":")) && waitingForResponse) {
            parsePluginList(text);
            waitingForResponse = false;
            analyzePlugins();
        }
        // Risposta "unknown command" -> il server nasconde /plugins
        else if (lower.contains("unknown command") || lower.contains("sconosciuto") || lower.contains("non esiste")) {
            if (waitingForResponse) {
                waitingForResponse = false;
                displayMessage("§7Il server nasconde §f/plugins§7 — uso l'analisi dei command dispatcher.");
                scanViaCommands();
            }
        }
    }

    public void scanViaCommands() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        try {
            var dispatcher = mc.getConnection().getCommands();
            if (dispatcher != null) {
                for (com.mojang.brigadier.tree.CommandNode<?> node : dispatcher.getRoot().getChildren()) {
                    String name = node.getName();
                    if (name != null && name.contains(":")) {
                        String plugin = name.split(":")[0];
                        if (!plugin.equals("minecraft") && !plugin.equals("bukkit") && !foundPlugins.contains(plugin)) {
                            foundPlugins.add(plugin);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        if (!foundPlugins.isEmpty()) {
            displayMessage("§8[§aPluginScanner§8] §fPlugin rilevati dai comandi: §7" + String.join(", ", foundPlugins));
            analyzePlugins();
        }
    }

    private void parsePluginList(String raw) {
        // Formato tipico bukkit: "Plugins (3): PluginA, PluginB, PluginC"
        int start = raw.toLowerCase(Locale.ROOT).indexOf("plugins");
        String rest = raw.substring(Math.min(start + 8, raw.length()));
        Matcher m = PLUGIN_LIST.matcher(rest);
        boolean hasColon = rest.contains(":");
        while (m.find()) {
            String name = m.group();
            if (hasColon && m.start() < rest.indexOf(':')) continue; // skip il "Plugins" stesso
            if (name.equalsIgnoreCase("plugins")) continue;
            if (name.equalsIgnoreCase("bukkit")) continue;
            if (name.equalsIgnoreCase("minecraft")) continue;
            if (!foundPlugins.contains(name)) foundPlugins.add(name);
        }
        // Remove eventuali collection na "Plugin:" captati
        foundPlugins.removeIf(n -> n.length() > 24);
    }

    private void analyzePlugins() {
        if (foundPlugins.isEmpty()) {
            displayMessage("§8[§bPluginScanner§8] §Non ho rilevato plugin (server nascosto).");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§ePluginServer: §7").append(foundPlugins.size()).append(" §f- §a");
        sb.append(String.join("§7, §a", foundPlugins));
        displayMessage(sb.toString());

        boolean permPlugin = false;
        for (String plug : foundPlugins) {
            String p = plug.toLowerCase(Locale.ROOT);
            if (PERMISSION_PLUGINS.containsKey(p)) {
                permPlugin = true;
                String[] probes = PERMISSION_PLUGINS.get(p);
                displayMessage("§e[PluginScanner] §fPlugin permessi: §b" + plug + "§f!");
                if (showSuggestions.isOn()) {
                    displayMessage("§7  Probe: §f/" + probes[1] + "§8 | §f/" + probes[2] + "§8 | §f/" + probes[0]);
                }
                if (autoProbe.isOn() && mc.getConnection() != null) {
                    try { mc.getConnection().sendCommand(probes[1]); } catch (Exception ignored) {}
                }
            }
        }
        if (!permPlugin) {
            displayMessage("§8[§bPluginScanner§8] §7Nessun plugin permessi noto tra quelli visibili. Usa §f/plugins§7 §8oppure §f/help§8.");
        }
    }

    private void displayMessage(String msg) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(msg), false);
        }
    }
}