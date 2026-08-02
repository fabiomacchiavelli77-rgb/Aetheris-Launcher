package net.aetheris.client.modules.impl.player;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import net.aetheris.client.settings.SliderSetting;

public class AutoFish extends Module {
    private final SliderSetting reelDelaySetting = new SliderSetting("reelDelay", "Reel Delay", "Ritardo Raccolta", 5.0, 0.0, 20.0, 1.0, "ticks");
    private final SliderSetting recastDelaySetting = new SliderSetting("recastDelay", "Recast Delay", "Ritardo Rilancio", 10.0, 0.0, 20.0, 1.0, "ticks");
    
    private boolean wasCasting = false;
    private int recastDelay = 0;
    private int reelDelay = 0;

    public AutoFish() {
        super("AutoFish", "Pesca automaticamente quando il pesce abbocca.", Category.PLAYER);
        addSetting(reelDelaySetting);
        addSetting(recastDelaySetting);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.gameMode == null) return;
        
        if (recastDelay > 0) { 
            recastDelay--; 
            return; 
        }
        
        if (reelDelay > 0) {
            reelDelay--;
            if (reelDelay == 0) {
                // Tira su il pesce
                InteractionHand hand = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                    ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                mc.gameMode.useItem(mc.player, hand);
                mc.player.swing(hand);
                recastDelay = recastDelaySetting.getValue().intValue();
                wasCasting = false;
            }
            return;
        }

        boolean holdingRod = mc.player.getMainHandItem().getItem() instanceof FishingRodItem
                          || mc.player.getOffhandItem().getItem() instanceof FishingRodItem;

        if (!holdingRod) return;

        if (mc.player.fishing != null) {
            var hook = mc.player.fishing;
            // Un pesce ha abboccato se l'amo subisce una spinta verso il basso (Y < -0.1) o se ha agganciato qualcosa
            boolean hasCaught = hook.getHookedIn() != null || hook.getDeltaMovement().y < -0.1;
            
            if (hasCaught && reelDelay == 0) {
                reelDelay = Math.max(1, reelDelaySetting.getValue().intValue());
            }
        } else if (!wasCasting && recastDelay == 0) {
            // Lancia la lenza se non c'è un amo attivo
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
