package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.HashSet;
import java.util.Set;

public class Xray extends Module {
    private static final Set<Block> XRAY_BLOCKS = new HashSet<>();

    /** Opacity of non-xray blocks: 0 = fully hidden, 100 = fully visible */
    private static int opacity = 0;

    /** Whether to apply Night Vision effect while Xray is active */
    private static boolean nightVisionEnabled = true;

    static {
        XRAY_BLOCKS.add(Blocks.DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.IRON_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        XRAY_BLOCKS.add(Blocks.COAL_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        XRAY_BLOCKS.add(Blocks.COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
        XRAY_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        XRAY_BLOCKS.add(Blocks.CHEST);
        XRAY_BLOCKS.add(Blocks.ENDER_CHEST);
        XRAY_BLOCKS.add(Blocks.TRAPPED_CHEST);
        XRAY_BLOCKS.add(Blocks.SPAWNER);
        XRAY_BLOCKS.add(Blocks.TNT);
        XRAY_BLOCKS.add(Blocks.BARREL);
    }

    public Xray() {
        super("Xray", "Mostra solo minerali e blocchi preziosi.", Category.RENDER);
    }

    // ── Opacity ──────────────────────────────────────────────────────
    public static int getOpacity() { return opacity; }

    public static void setOpacity(int value) {
        opacity = Math.max(0, Math.min(100, value));
        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    /** Returns the opacity as a float factor 0.0–1.0 */
    public static float getOpacityFactor() { return opacity / 100f; }

    // ── Night Vision ─────────────────────────────────────────────────
    public static boolean isNightVisionEnabled() { return nightVisionEnabled; }

    public static void setNightVisionEnabled(boolean enabled) { nightVisionEnabled = enabled; }

    // ── Block management ─────────────────────────────────────────────
    public static boolean isXrayBlock(Block block) { return XRAY_BLOCKS.contains(block); }

    public static Set<Block> getXrayBlocks() { return XRAY_BLOCKS; }

    public static void toggleXrayBlock(Block block) {
        if (XRAY_BLOCKS.contains(block)) {
            XRAY_BLOCKS.remove(block);
        } else {
            XRAY_BLOCKS.add(block);
        }
        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    public static void resetDefaultBlocks() {
        XRAY_BLOCKS.clear();
        XRAY_BLOCKS.add(Blocks.DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        XRAY_BLOCKS.add(Blocks.EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        XRAY_BLOCKS.add(Blocks.GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.IRON_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        XRAY_BLOCKS.add(Blocks.COAL_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        XRAY_BLOCKS.add(Blocks.COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        XRAY_BLOCKS.add(Blocks.LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        XRAY_BLOCKS.add(Blocks.REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        XRAY_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        XRAY_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
        XRAY_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        XRAY_BLOCKS.add(Blocks.CHEST);
        XRAY_BLOCKS.add(Blocks.ENDER_CHEST);
        XRAY_BLOCKS.add(Blocks.TRAPPED_CHEST);
        XRAY_BLOCKS.add(Blocks.SPAWNER);
        XRAY_BLOCKS.add(Blocks.TNT);
        XRAY_BLOCKS.add(Blocks.BARREL);
        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    public static void clearAllBlocks() {
        XRAY_BLOCKS.clear();
        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    public static void selectAllBlocks(Iterable<Block> blocks) {
        for (Block b : blocks) {
            XRAY_BLOCKS.add(b);
        }
        if (net.minecraft.client.Minecraft.getInstance().levelRenderer != null) {
            net.minecraft.client.Minecraft.getInstance().levelRenderer.allChanged();
        }
    }

    @Override
    public void onTick() {
        if (mc.player != null && nightVisionEnabled) {
            mc.player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.NIGHT_VISION, 520, 0, false, false, false
            ));
        }
    }

    @Override
    public void onEnable() {
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        }
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }
}
