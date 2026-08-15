package net.aetheris.client.modules.impl.movement;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.Vec3;

/**
 * Gesu — permette di camminare sulla superficie dell'acqua.
 *
 * Logica (per tick):
 *  - Se i piedi sono in acqua e vicini alla superficie: azzera la velocità
 *    verticale in discesa, azzera fallDistance e afferma onGround -> il player
 *    resta "in piedi" sul pelo dell'acqua e può camminare orizzontalmente.
 *  - Se immerso sotto la superficie: spinge verso l'alto per riemergere
 *    (disattivabile con Sneak se diveOnSneak è attivo).
 *  - Jump: dà l'impulso di salto normale per lasciare la superficie.
 *  - surfaceSpeed: moltiplicatore orizzontale per compensare la lentezza
 *    del nuoto (1.0-2.0).
 */
public class Gesu extends Module {

    private final SliderSetting surfaceSpeed = new SliderSetting(
            "surfaceSpeed", "Surface Speed", "Velocità sull'acqua", 1.3, 1.0, 2.0, 0.1, "x");
    private final BooleanSetting diveOnSneak = new BooleanSetting(
            "diveOnSneak", "Dive on Sneak", "Sneak = immergiti invece di riemergere", true);

    public Gesu() {
        super("Gesu", "Cammina sulla superficie dell'acqua.", Category.MOVEMENT);
        addSetting(surfaceSpeed);
        addSetting(diveOnSneak);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (mc.player.isSpectator() || mc.player.getAbilities().flying) return;

        Vec3 mov = mc.player.getDeltaMovement();

        // Salto: impulso verticale normale per lasciare la superficie
        if (mc.options.keyJump.isDown()) {
            mc.player.setDeltaMovement(mov.x, 0.42, mov.z);
            return;
        }

        // Sneak: lascia immergere (se l'impostazione è attiva)
        boolean sneaking = mc.player.isShiftKeyDown();
        if (sneaking && diveOnSneak.isOn()) return;

        BlockPos feetPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        if (!mc.level.getFluidState(feetPos).is(FluidTags.WATER)) return;

        // Superficie dell'acqua sorgente (altezza fluido ~0.888 blocchi)
        double surfaceY = feetPos.getY() + 0.888;
        double feetY = mc.player.getY();

        if (feetY >= surfaceY - 0.1) {
            // Sulla superficie: stai in piedi
            mc.player.fallDistance = 0f;
            mc.player.setOnGround(true);
            double vy = mov.y < 0 ? 0 : Math.min(mov.y, 0.05);
            boolean moving = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                    || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
            double boost = moving ? surfaceSpeed.getValue() : 1.0;
            mc.player.setDeltaMovement(mov.x * boost, vy, mov.z * boost);
        } else {
            // Immerso: riemergi (a meno che sneak non tenga giù)
            mc.player.fallDistance = 0f;
            mc.player.setDeltaMovement(mov.x, Math.max(mov.y, 0.25), mov.z);
        }
    }
}
