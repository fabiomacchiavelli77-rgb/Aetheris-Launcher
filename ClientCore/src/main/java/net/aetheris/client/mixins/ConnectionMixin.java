package net.aetheris.client.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.aetheris.client.modules.impl.render.FreeCam;
import net.aetheris.client.modules.impl.world.PacketLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hook di rete su Connection (outbound send + inbound channelRead0).
 *
 * - PacketLogger: log outbound (send) e inbound (channelRead0).
 * - FreeCam: blocca i pacchetti di movimento/interazione mentre attivo.
 * - NoFall: riscrive i ServerboundMovePlayerPacket con onGround=true quando si cade.
 *
 * I riferimenti ai moduli sono cached (lazy, volatile): il vecchio codice iterava
 * tutti i 61 moduli per OGNI pacchetto inviato/ricevuto.
 */
@Mixin(Connection.class)
public class ConnectionMixin {

    private static volatile PacketLogger cachedPacketLogger;
    private static volatile FreeCam cachedFreeCam;
    private static volatile NoFall cachedNoFall;

    private static PacketLogger packetLogger() {
        PacketLogger pl = cachedPacketLogger;
        if (pl == null) { pl = ModuleManager.getModule(PacketLogger.class); cachedPacketLogger = pl; }
        return pl;
    }

    private static FreeCam freeCam() {
        FreeCam fc = cachedFreeCam;
        if (fc == null) { fc = ModuleManager.getModule(FreeCam.class); cachedFreeCam = fc; }
        return fc;
    }

    private static NoFall noFall() {
        NoFall nf = cachedNoFall;
        if (nf == null) { nf = ModuleManager.getModule(NoFall.class); cachedNoFall = nf; }
        return nf;
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketLogger pl = packetLogger();
        if (pl != null && pl.isEnabled()) {
            pl.onSendPacket(packet);
        }

        FreeCam fc = freeCam();
        if (fc != null && fc.isEnabled()) {
            if (packet instanceof ServerboundMovePlayerPacket ||
                packet instanceof ServerboundPlayerCommandPacket ||
                packet instanceof ServerboundSwingPacket ||
                packet instanceof ServerboundInteractPacket ||
                packet instanceof ServerboundUseItemPacket ||
                packet instanceof ServerboundPlayerActionPacket) {
                ci.cancel();
            }
        }
    }

    /** Hook inbound: in 1.21.4 tutti i pacchetti in arrivo passano da channelRead0 (SimpleChannelInboundHandler). */
    @Inject(method = "channelRead0", at = @At("HEAD"))
    private void onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketLogger pl = packetLogger();
        if (pl != null && pl.isEnabled()) {
            pl.onReceivePacket(packet);
        }
    }

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> modifyPacketForNoFall(Packet<?> packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket movePacket)) return packet;

        NoFall nf = noFall();
        if (nf == null || !nf.isEnabled()) return packet;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator()) return packet;

        // Se la velocità verso il basso è significativa (stiamo cadendo),
        // modifichiamo il pacchetto per dire al server che siamo a terra.
        if (mc.player.getDeltaMovement().y >= -0.4) return packet;

        if (movePacket instanceof ServerboundMovePlayerPacket.PosRot p) {
            return new ServerboundMovePlayerPacket.PosRot(p.getX(0), p.getY(0), p.getZ(0), p.getYRot(0), p.getXRot(0), true, p.horizontalCollision());
        } else if (movePacket instanceof ServerboundMovePlayerPacket.Pos p) {
            return new ServerboundMovePlayerPacket.Pos(p.getX(0), p.getY(0), p.getZ(0), true, p.horizontalCollision());
        } else if (movePacket instanceof ServerboundMovePlayerPacket.Rot p) {
            return new ServerboundMovePlayerPacket.Rot(p.getYRot(0), p.getXRot(0), true, p.horizontalCollision());
        } else if (movePacket instanceof ServerboundMovePlayerPacket.StatusOnly p) {
            return new ServerboundMovePlayerPacket.StatusOnly(true, p.horizontalCollision());
        }
        return packet;
    }
}
