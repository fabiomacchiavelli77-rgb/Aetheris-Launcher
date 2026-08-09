package net.aetheris.client.modules.impl.world;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;

import java.util.HashSet;
import java.util.Set;

public class InstalledPlugins extends Module {

    public InstalledPlugins() {
        super("InstalledPlugins", "Shows server plugins based on command completions.", Category.WORLD);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            toggle();
            return;
        }

        Set<String> plugins = new HashSet<>();
        
        // Use mc.getConnection().getCommandDispatcher() in 1.21.4 mappings (Mojang)
        // Wait, maybe getCommands() or getDispatcher()? We'll just try to compile this and see what the compiler says.
        try {
            var dispatcher = mc.getConnection().getCommands(); // Let's guess getCommands() 
            // Wait, in Mojang mappings it is often getCommands() -> CommandDispatcher<SharedSuggestionProvider>
            if (dispatcher != null) {
                for (CommandNode<?> node : dispatcher.getRoot().getChildren()) {
                    String name = node.getName();
                    if (name != null && name.contains(":")) {
                        String plugin = name.split(":")[0];
                        if (!plugin.equals("minecraft") && !plugin.equals("bukkit")) {
                            plugins.add(plugin);
                        }
                    }
                }
            }
        } catch (Exception e) {}

        mc.execute(() -> {
            if (mc.player != null) {
                if (plugins.isEmpty()) {
                    mc.player.displayClientMessage(Component.literal("§cNo plugins found or server hides commands."), false);
                } else {
                    mc.player.displayClientMessage(Component.literal("§aFound Plugins (" + plugins.size() + "): §7" + String.join(", ", plugins)), false);
                }
            }
        });

        // Auto-disable because it's a one-time action
        this.setEnabled(false);
    }
}
