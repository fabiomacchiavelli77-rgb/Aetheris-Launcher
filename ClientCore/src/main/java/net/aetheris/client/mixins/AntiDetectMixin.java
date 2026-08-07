package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.player.AntiDetect;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AntiDetect - nasconde la presenza di Fabric dai plugin anti-mod.
 *
 * Due interventi su Connection.send (copre play e configuration phase):
 *
 * 1. Brand spoof: il payload minecraft:brand (BrandPayload) e' il segnale
 *    principale che identifica un client Fabric ("fabric" invece di "vanilla").
 *    Lo riscriviamo in "vanilla" (ClientBrandRetriever.VANILLA_NAME).
 *
 * 2. Blocco payload fabric:*: in 1.21.4 Fabric comunica col server con
 *    payload custom di namespace "fabric" (fabric:register, fabric:version, ...).
 *    Un client vanilla non li invia mai, quindi vengono cancellati.
 *    (Nota: il RegisterPayload vanilla non esiste piu' in 1.21.4.)
 */
@Mixin(Connection.class)
public class AntiDetectMixin {

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> spoofBrandPayload(Packet<?> packet) {
        if (!(packet instanceof ServerboundCustomPayloadPacket cp)) return packet;
        if (!(cp.payload() instanceof BrandPayload)) return packet;

        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof AntiDetect ad && ad.isEnabled()) {
                return new ServerboundCustomPayloadPacket(new BrandPayload(ClientBrandRetriever.VANILLA_NAME));
            }
        }
        return packet;
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void blockFabricPayloads(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundCustomPayloadPacket cp)) return;

        for (var mod : ModuleManager.getModules()) {
            if (mod instanceof AntiDetect ad && ad.isEnabled()) {
                if (cp.payload().type().id().getNamespace().equals("fabric")) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
