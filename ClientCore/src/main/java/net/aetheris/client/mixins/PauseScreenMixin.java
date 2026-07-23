package net.aetheris.client.mixins;

import kaptainwutax.seedcrackerX.config.ConfigScreen;
import net.aetheris.client.gui.AetherisMenuScreen;
import net.aetheris.client.gui.AltManagerScreen;
import net.aetheris.client.gui.KeybindManagerScreen;
import net.aetheris.client.gui.XrayBlockSelectorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {

    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        int btnWidth = 100;
        int btnHeight = 20;
        int startY = 12;

        // Aetheris Menu button
        this.addRenderableWidget(Button.builder(Component.literal("§d§lAetheris Menu"), b -> {
            Minecraft.getInstance().setScreen(new AetherisMenuScreen());
        }).bounds(10, startY, btnWidth, btnHeight).build());

        // SeedCracker Config button
        this.addRenderableWidget(Button.builder(Component.literal("§e§lSeedCracker"), b -> {
            Minecraft.getInstance().setScreen(new net.aetheris.client.gui.SeedCrackerConfigScreen(this));
        }).bounds(115, startY, btnWidth, btnHeight).build());

        // Xray Ores button
        this.addRenderableWidget(Button.builder(Component.literal("§6§lXray Ores"), b -> {
            Minecraft.getInstance().setScreen(new XrayBlockSelectorScreen(this));
        }).bounds(220, startY, btnWidth, btnHeight).build());

        // Alt Manager button
        this.addRenderableWidget(Button.builder(Component.literal("§b§lAlt Manager"), b -> {
            Minecraft.getInstance().setScreen(new AltManagerScreen(this));
        }).bounds(325, startY, btnWidth, btnHeight).build());
    }
}
