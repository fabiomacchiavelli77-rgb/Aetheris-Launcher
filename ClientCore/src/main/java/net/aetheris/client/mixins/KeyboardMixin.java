package net.aetheris.client.mixins;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.gui.AetherisMenuScreen;
import net.aetheris.client.gui.ClickGUI;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return; // Non processare keybind se un menu è aperto

        // Right Shift = apre menu Aetheris
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            mc.setScreen(new AetherisMenuScreen());
            return;
        }

        // Left Shift = apre ClickGUI
        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            mc.setScreen(new ClickGUI());
            return;
        }

        // Processa keybind dei moduli
        for (Module module : ModuleManager.getModules()) {
            if (module.getKeybind() == key) {
                module.toggle();
                ProfileManager.getInstance().onModuleChanged();
            }
        }
    }
}
