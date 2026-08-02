package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            for (var mod : ModuleManager.getModules()) {
                if (mod instanceof NoFall nf && nf.isEnabled()) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                        if (mc.player.fallDistance > nf.getFallThreshold()) {
                            ((ServerboundMovePlayerPacketAccessor) movePacket).setOnGround(true);
                            mc.player.fallDistance = 0f;
                        }
                    }
                }
            }
        }
    }
}
