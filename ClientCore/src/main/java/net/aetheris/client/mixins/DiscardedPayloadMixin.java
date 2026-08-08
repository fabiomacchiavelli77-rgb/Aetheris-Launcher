package net.aetheris.client.mixins;

import net.aetheris.client.modules.impl.world.PluginScanner;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * PluginScanner - sniffa i canali registrati dal server (minecraft:register).
 *
 * In 1.21.11 il payload di registrazione arriva come DiscardedPayload e il buffer
 * viene scartato: il factory privato method_56491(int, Identifier, FriendlyByteBuf)
 * legge la lista di channel. Lo intercettiamo qui, leggiamo i nomi e ripristiniamo
 * la posizione del buffer, poi il client continua indisturbato.
 */
@Mixin(DiscardedPayload.class)
public class DiscardedPayloadMixin {

    @Inject(method = "method_56491", at = @At("HEAD"))
    private static void onDecodeRegisterPayload(int id, Identifier payloadId, FriendlyByteBuf buf, CallbackInfoReturnable<DiscardedPayload> cir) {
        if (!payloadId.getNamespace().equals("minecraft") || !payloadId.getPath().equals("register")) return;
        PluginScanner ps = PluginScanner.getInstance();
        if (ps == null || !ps.isEnabled()) return;

        List<String> channels = new ArrayList<>();
        int mark = buf.readerIndex();
        try {
            while (buf.readableBytes() > 0) {
                channels.add(buf.readUtf());
            }
        } catch (Exception ignored) {
        } finally {
            buf.readerIndex(mark);
        }
        if (!channels.isEmpty()) {
            ps.onRegisterChannels(channels);
        }
    }
}
