package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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

    /**
     * Firma-DB di plugin noti (solo dati pubblici: comandi, channel, brand).
     * Chiave = match case-insensitive su nome/alias/channel namespace.
     * Serve a categorizzare il report: permessi, anti-cheat, world-edit, economia.
     */
    private static final Map<String, String> PLUGIN_CATEGORIES = new LinkedHashMap<>();
    static {
        // Gestione permessi
        for (String k : new String[]{"luckperms", "permissionsex", "pex", "groupmanager",
                "permissionsbukkit", "zpermissions", "bpermissions", "ultrapermissions", "vault"})
            PLUGIN_CATEGORIES.put(k, "Permessi");
        // Anti-cheat (informazione utile: sapere quale AC gira è pubblico)
        for (String k : new String[]{"grim", "grimac", "vulcan", "matrix", "polar", "verus",
                "aac", "negativity", "spartan", "hawk", "intuition", "hecca"})
            PLUGIN_CATEGORIES.put(k, "Anti-cheat");
        // World management / build
        for (String k : new String[]{"worldedit", "fastasyncworldedit", "fawe", "worldguard", "go brush", "gobrush", "ares"})
            PLUGIN_CATEGORIES.put(k, "World-edit");
        // Economia / shop
        for (String k : new String[]{"essentials", "essentialsx", "essentialschat", "cmi", "vaulteco",
                "shopguiplus", "chestshop", "quickshop", "playerpoints"})
            PLUGIN_CATEGORIES.put(k, "Economia");
        // Moderazione / logging
        for (String k : new String[]{"coreprotect", "litebans", "advancedban"})
            PLUGIN_CATEGORIES.put(k, "Moderazione");
        // Proxy / performance
        for (String k : new String[]{"bungeecord", "velocity", "spark", "timings", "placeholderapi", "viaversion", "protocollib"})
            PLUGIN_CATEGORIES.put(k, "Infrastruttura");
    }

    /** Comandi vanilla/Bukkit: esclusi dal tab-probe per non inquinare il report. */
    private static final Set<String> VANILLA_COMMANDS = new HashSet<>(List.of(
            "help", "msg", "teammsg", "say", "me", "tell", "trigger", "seed", "list",
            "advance", "attribute", "bossbar", "clear", "clone", "damage", "data", "datapack",
            "debug", "defaultgamemode", "deop", "difficulty", "effect", "enchant", "execute",
            "experience", "xp", "fill", "fillbiome", "forceload", "function", "gamemode",
            "gamerule", "give", "item", "jfr", "kick", "kill", "locate", "loot", "op",
            "pardon", "particle", "playsound", "publish", "recipe", "reload", "replaceitem",
            "ride", "rotate", "save-all", "save-off", "save-on", "schedule", "scoreboard",
            "setblock", "setidletimeout", "setworldspawn", "spawnpoint", "spectate",
            "spreadplayers", "stopsound", "summon", "tag", "team", "teleport", "tp",
            "tellraw", "testfor", "testforblock", "testforblocks", "tick", "time", "title",
            "toggledownfall", "transfer", "weather", "whitelist", "worldborder", "place",
            "random", "return", "seed", "reload", "ban", "ban-ip", "banlist", "stop",
            "plugins", "pl", "version", "ver", "about", "icanhasbukkit", "bukkit", "calc",
            "console", "inventory", "invsee", "motd", "save", "tps", "essentials", "setworth"
    ));

    private final List<String> foundPlugins = new ArrayList<>();
    private boolean waitingForResponse = false;
    private int tickCounter = 0;

    // ---- Tab-probe (enumera comandi via CommandSuggestions, senza /plugins) ----
    private final Queue<String> tabQueries = new ArrayDeque<>();
    private boolean probingTab = false;
    private int tabTick = 0;
    private int tabSequence = 0;
    private String serverBrand = null;

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
        serverBrand = null;
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
        if (probingTab) {
            tabTick++;
            // Un query ogni 4 tick: evita flood che i plugin anti-cheat potrebbero flaggare.
            if (tabTick % 4 == 0 && !tabQueries.isEmpty()) {
                sendTabQuery(tabQueries.poll());
            }
            if (tabQueries.isEmpty()) {
                probingTab = false;
                if (!foundPlugins.isEmpty()) {
                    displayMessage("§8[§bPluginScanner§8] §fTab-probe completato: §a" + String.join("§7, §a", foundPlugins));
                }
                analyzePlugins();
            }
        }
        if (!waitingForResponse) return;
        tickCounter++;
        // Timeout 3 secondi: il server potrebbe nascondere /plugins
        if (tickCounter > 60 && !foundPlugins.isEmpty()) {
            waitingForResponse = false;
        }
        // Timeout 5 secondi senza risposta -> fallback al tab-probe
        if (tickCounter > 100) {
            waitingForResponse = false;
            displayMessage("§7Nessuna risposta a /plugins — avvio tab-probe.");
            startTabProbe();
        }
    }

    /**
     * Enumera i comandi registrati via packet CommandSuggestions (tab-completion).
     * Non esegue alcun comando: invia solo richieste di completamento ("", a..z, 0..9)
     * e legge la lista dei comandi nella risposta. I server Bukkit/Paper rispondono
     * anche quando /plugins e' nascosto o disabilitato.
     */
    public void startTabProbe() {
        if (probingTab || mc.getConnection() == null) return;
        probingTab = true;
        tabTick = 0;
        tabQueries.clear();
        tabQueries.add("");
        for (char c = 'a'; c <= 'z'; c++) tabQueries.add(String.valueOf(c));
        for (char c = '0'; c <= '9'; c++) tabQueries.add(String.valueOf(c));
        displayMessage("§8[§bPluginScanner§8] §7Tab-probe avviato (enumera comandi, niente /plugins)...");
    }

    private void sendTabQuery(String query) {
        if (mc.getConnection() == null) { probingTab = false; return; }
        try {
            mc.getConnection().send(new ServerboundCommandSuggestionPacket(tabSequence++, query));
        } catch (Exception ignored) { probingTab = false; }
    }

    /** Chiamato dal mixin quando il server risponde a una richiesta di completamento. */
    public void onCommandSuggestions(ClientboundCommandSuggestionsPacket packet) {
        if (!isEnabled()) return;
        Set<String> fresh = new HashSet<>();
        for (ClientboundCommandSuggestionsPacket.Entry e : packet.suggestions()) {
            String text = e.text();
            if (text == null) continue;
            String token = text.trim().split(" ")[0];
            if (token.isEmpty()) continue;
            if (token.contains(":")) {
                // Comando namespaced: "essentials:fly" -> plugin "essentials"
                String ns = token.split(":")[0];
                if (!ns.equals("minecraft") && !ns.equals("bukkit") && !ns.equals("spigot")) {
                    fresh.add(ns);
                }
            } else if (!VANILLA_COMMANDS.contains(token.toLowerCase(Locale.ROOT))) {
                // Comando root non-vanilla: alias plugin (es. "lp", "fly", "heal")
                fresh.add(token);
            }
        }
        if (fresh.isEmpty()) return;
        boolean added = false;
        for (String f : fresh) {
            if (!foundPlugins.contains(f)) { foundPlugins.add(f); added = true; }
        }
        probingTab = false;
        tabQueries.clear();
        displayMessage("§8[§bPluginScanner§8] §fComandi rilevati: §7" + String.join("§7, §a", fresh));
        analyzePlugins();
    }

    /** Chiamato dal mixin: sniffa il brand del server (minecraft:brand). */
    public void onCustomPayload(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (!isEnabled()) return;
        try {
            if (payload instanceof BrandPayload brand) {
                onBrand(brand.brand());
            }
        } catch (Exception ignored) {}
    }

    private void onBrand(String brand) {
        if (brand == null || brand.isEmpty() || brand.equals(serverBrand)) return;
        serverBrand = brand;
        String lower = brand.toLowerCase(Locale.ROOT);
        boolean pluginServer = lower.contains("paper") || lower.contains("spigot")
                || lower.contains("purpur") || lower.contains("folia");
        displayMessage("§8[§bPluginScanner§8] §fServer software: §a" + brand
                + (pluginServer ? " §7(server a plugin)" : ""));
    }

    public void onRegisterChannels(List<String> channels) {
        boolean added = false;
        List<String> shown = new ArrayList<>();
        for (String ch : channels) {
            if (!ch.contains(":")) continue;
            String ns = ch.split(":")[0];
            if (ns.equals("minecraft") || ns.equals("bukkit") || ns.equals("fabric")) continue;
            if (ns.equals("bungeecord")) {
                displayMessage("§8[§bPluginScanner§8] §fProxy rilevato: §aBungeeCord §7(plugin channels attivi)");
            }
            if (!foundPlugins.contains(ch)) {
                foundPlugins.add(ch);
                shown.add(ch);
                added = true;
            }
        }
        if (added) {
            displayMessage("§8[§bPluginScanner§8] §fPlugin channels: §7" + String.join("§7, §a", shown));
            analyzePlugins();
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
                displayMessage("§7Il server nasconde §f/plugins§7 — uso l'analisi dei command dispatcher + tab-probe.");
                scanViaCommands();
                startTabProbe();
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
            displayMessage("§8[§bPluginScanner§8] §7Nessun plugin rilevato (server nascosto).");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§8[§bPluginScanner§8] §ePlugin trovati: §7").append(foundPlugins.size()).append(" §f- §a");
        sb.append(String.join("§7, §a", foundPlugins));
        displayMessage(sb.toString());

        reportCategories();

        boolean permPlugin = false;
        for (String plug : foundPlugins) {
            String p = plug.toLowerCase(Locale.ROOT);
            String[] probes = null;
            for (Map.Entry<String, String[]> e : PERMISSION_PLUGINS.entrySet()) {
                String k = e.getKey();
                // match esatto, contenuto o prefisso breve ("lp" -> luckperms, "pex" -> permissionsex)
                if (p.equals(k) || p.contains(k) || (p.length() >= 2 && k.startsWith(p))) {
                    probes = e.getValue();
                    break;
                }
            }
            if (probes != null) {
                permPlugin = true;
                displayMessage("§8[§bPluginScanner§8] §fPlugin permessi: §b" + plug);
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

    /**
     * Report categorizzato basato sulla firma-DB (nome/alias/channel -> categoria).
     * Evidenzia in particolare gli anti-cheat attivi: informazione pubblica,
     * utile per capire il livello di enforcement del server.
     */
    private void reportCategories() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        List<String> antiCheats = new ArrayList<>();
        for (String plug : foundPlugins) {
            String category = lookupCategory(plug);
            if (category == null) continue;
            counts.merge(category, 1, Integer::sum);
            if (category.equals("Anti-cheat")) antiCheats.add(plug);
        }
        if (!counts.isEmpty()) {
            StringBuilder sb = new StringBuilder("§8[§bPluginScanner§8] §7Categorie: ");
            boolean first = true;
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                if (!first) sb.append("§8 | §7");
                sb.append("§f").append(e.getKey()).append("§8(§7").append(e.getValue()).append("§8)");
                first = false;
            }
            displayMessage(sb.toString());
        }
        if (!antiCheats.isEmpty()) {
            displayMessage("§8[§bPluginScanner§8] §6Anti-cheat rilevati: §e" + String.join("§7, §e", antiCheats));
        }
    }

    /** Match esatto, contenuto o prefisso tra nome plugin e firma-DB. */
    private String lookupCategory(String plugin) {
        String p = plugin.toLowerCase(Locale.ROOT);
        if (p.length() < 3) return null; // evita match ambigui su nomi corti
        for (Map.Entry<String, String> e : PLUGIN_CATEGORIES.entrySet()) {
            String key = e.getKey();
            if (p.equals(key) || p.contains(key) || (key.contains(p) && p.length() >= 4)) {
                return e.getValue();
            }
        }
        return null;
    }

    private void displayMessage(String msg) {
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal(msg), false);
            }
        });
    }
}
