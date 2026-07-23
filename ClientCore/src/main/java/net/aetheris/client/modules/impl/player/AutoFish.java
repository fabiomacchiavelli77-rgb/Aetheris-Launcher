package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;

public class AutoFish extends Module {
    private boolean wasCasting = false;
    private int recastDelay = 0;

    public AutoFish() {
        super("AutoFish", "Pesca automaticamente quando il pesce abbocca.", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (recastDelay > 0) { recastDelay--; return; }

        boolean holdingRod = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                          || mc.player.getOffhandItem().getItem() instanceof FishingRodItem;

        if (!holdingRod) return;

        // Controlla se il galleggiante ha preso qualcosa
        if (mc.player.fishing != null) {
            boolean hasCaught = mc.player.fishing.getHookedIn() != null;
            if (hasCaught) {
                // Tira su il pesce (right-click per ritrarre la lenza)
                InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                    ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                mc.gameMode.useItem(mc.player, hand);
                mc.player.swing(hand);
                recastDelay = 15;

                // Rilancia dopo un attimo
                wasCasting = false;
            }
        } else if (!wasCasting && recastDelay == 0) {
            // Lancia la lenza
            InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            mc.gameMode.useItem(mc.player, hand);
            mc.player.swing(hand);
            wasCasting = true;
        }

        if (mc.player.fishing == null) {
            wasCasting = false;
        }
    }
}
