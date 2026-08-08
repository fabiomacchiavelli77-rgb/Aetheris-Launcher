package net.aetheris.client.mixins;

import net.aetheris.client.modules.ModuleManager;
import net.aetheris.client.modules.impl.player.NoChatReports;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * NoChatReports - rimuove le firme crittografiche dai messaggi di chat/comandi in uscita.
 *
 * In 1.21.4 ogni messaggio chat firmato (MessageSignature) permette al server di
 * verificare l'identita' del mittente e ai plugin anti-hack di tracciare i comandi
 * firmati. Strippando la firma il messaggio arriva come "non firmato": il server
 * vanilla lo accetta comunque, ma plugin di moderation/anticheat non possono piu'
 * correlare la firma al giocatore.
 *
 * - ServerboundChatPacket        -> ricreato con signature=null
 * - ServerboundChatCommandSignedPacket -> downgrade a ServerboundChatCommandPacket
 *   (che in 1.21.4 non trasporta firme di argomenti).
 */
@Mixin(Connection.class)
public class ChatSignatureMixin {

    private static volatile NoChatReports cachedNoChatReports;

    private static NoChatReports noChatReports() {
        NoChatReports ncr = cachedNoChatReports;
        if (ncr == null) { ncr = ModuleManager.getModule(NoChatReports.class); cachedNoChatReports = ncr; }
        return ncr;
    }

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> stripChatSignatures(Packet<?> packet) {
        NoChatReports ncr = noChatReports();
        if (ncr == null || !ncr.isEnabled() || !ncr.isStripSignatures()) return packet;

        if (packet instanceof ServerboundChatPacket chat) {
            return new ServerboundChatPacket(chat.message(), chat.timeStamp(), chat.salt(), null, chat.lastSeenMessages());
        }
        if (packet instanceof ServerboundChatCommandSignedPacket cmd) {
            return new ServerboundChatCommandPacket(cmd.command());
        }
        return packet;
    }
}
