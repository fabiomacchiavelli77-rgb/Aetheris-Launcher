package net.aetheris.client.mixins;

import net.aetheris.client.gui.AltManagerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("§b§lAlt Manager"), b -> {
            Minecraft.getInstance().setScreen(new AltManagerScreen(this));
        }).bounds(10, 10, 95, 20).build());
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        guiGraphics.drawString(
            Minecraft.getInstance().font,
            "§d§lAetheris Client v1.0 (1.21.4)",
            110, 15,
            0xFFaa00aa
        );
    }
}
