package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.combat.Velocity;
import net.aetheris.client.modules.impl.world.PluginScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class AetherisClientPacketListenerMixin {

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void onHandleSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && packet.getId() == mc.player.getId()) {
            Velocity velocity = ModuleManager.getModule(Velocity.class);
            if (velocity != null && velocity.isEnabled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        PluginScanner ps = PluginScanner.getInstance();
        if (ps != null) {
            Component content = packet.content();
            if (content != null) {
                ps.onSystemChat(content);
            }
        }
    }

    /** Risposta a una richiesta di tab-completion: PluginScanner enumera i comandi (niente /plugins). */
    @Inject(method = "handleCommandSuggestions", at = @At("HEAD"))
    private void onCommandSuggestions(ClientboundCommandSuggestionsPacket packet, CallbackInfo ci) {
        PluginScanner ps = PluginScanner.getInstance();
        if (ps != null && ps.isEnabled()) {
            ps.onCommandSuggestions(packet);
        }
    }

    /** Payload custom inbound: PluginScanner sniffa brand server (minecraft:brand) e channel (minecraft:register). */
    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    private void onCustomPayload(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload, CallbackInfo ci) {
        PluginScanner ps = PluginScanner.getInstance();
        if (ps != null && ps.isEnabled()) {
            ps.onCustomPayload(payload);
        }
    }
}
