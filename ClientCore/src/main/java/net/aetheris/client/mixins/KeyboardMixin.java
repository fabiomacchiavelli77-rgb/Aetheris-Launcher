package net.aetheris.client.mixins;

import net.aetheris.client.config.ProfileManager;
import net.aetheris.client.gui.AetherisMenuScreen;
import net.aetheris.client.gui.ClickGUI;
import net.aetheris.client.modules.Module;
import net.aetheris.client.modules.ModuleManager;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (action != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return; // Non processare keybind se un menu è aperto
        
        int key = event.key();

        // Right Shift = apre ClickGUI (nuovo menu)
        // Right Ctrl + Right Shift = apre AetherisMenuScreen (vecchio menu)
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            boolean isRightCtrlDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            if (isRightCtrlDown) {
                mc.setScreen(new AetherisMenuScreen());
            } else {
                mc.setScreen(new ClickGUI());
            }
            ci.cancel();
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

