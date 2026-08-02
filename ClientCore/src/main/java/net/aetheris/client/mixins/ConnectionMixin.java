package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.movement.NoFall;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Connection.class)
public class ConnectionMixin {

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> modifyPacketForNoFall(Packet<?> packet) {
        if (packet instanceof ServerboundMovePlayerPacket movePacket) {
            for (var mod : ModuleManager.getModules()) {
                if (mod instanceof NoFall nf && nf.isEnabled()) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && !mc.player.isCreative() && !mc.player.isSpectator()) {
                        // Se la velocità verso il basso è significativa (stiamo cadendo),
                        // modifichiamo il pacchetto per dire al server che siamo a terra.
                        if (mc.player.getDeltaMovement().y < -0.4) {
                            if (movePacket instanceof ServerboundMovePlayerPacket.PosRot p) {
                                return new ServerboundMovePlayerPacket.PosRot(p.getX(0), p.getY(0), p.getZ(0), p.getYRot(0), p.getXRot(0), true, p.horizontalCollision());
                            } else if (movePacket instanceof ServerboundMovePlayerPacket.Pos p) {
                                return new ServerboundMovePlayerPacket.Pos(p.getX(0), p.getY(0), p.getZ(0), true, p.horizontalCollision());
                            } else if (movePacket instanceof ServerboundMovePlayerPacket.Rot p) {
                                return new ServerboundMovePlayerPacket.Rot(p.getYRot(0), p.getXRot(0), true, p.horizontalCollision());
                            } else if (movePacket instanceof ServerboundMovePlayerPacket.StatusOnly p) {
                                return new ServerboundMovePlayerPacket.StatusOnly(true, p.horizontalCollision());
                            }
                        }
                    }
                }
            }
        }
        return packet;
    }
}
