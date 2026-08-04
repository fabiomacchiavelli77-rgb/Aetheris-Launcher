package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.world.AutoSign;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AutoSignMixin {

    @Unique
    private boolean aetheris$filled = false;

    @Accessor("text")
    public abstract SignText getText();

    @Accessor("text")
    public abstract void setText(SignText value);

    @Accessor("messages")
    public abstract String[] getMessages();

    @Invoker("onDone")
    public abstract void callOnDone();

    @Inject(method = "init", at = @At("RETURN"))
    private void autoFillSign(CallbackInfo ci) {
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof AutoSign as && as.isEnabled()) {
                String[] messages = getMessages();
                SignText text = getText();
                for (int i = 0; i < 4; i++) {
                    String line = as.getLine(i);
                    if (line == null || line.isEmpty()) break;
                    messages[i] = line;
                    text = text.setMessage(i, Component.literal(line));
                }
                setText(text);
                aetheris$filled = true;
                return;
            }
        }
    }

    // onDone() viene chiamato nel primo tick, dopo che lo screen è completamente inizializzato
    @Inject(method = "tick", at = @At("HEAD"))
    private void confirmSign(CallbackInfo ci) {
        if (!aetheris$filled) return;
        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof AutoSign as && as.isEnabled()) {
                callOnDone();
                aetheris$filled = false;
                return;
            }
        }
        aetheris$filled = false;
    }
}
