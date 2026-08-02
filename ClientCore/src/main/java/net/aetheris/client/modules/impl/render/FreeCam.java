package net.aetheris.client.modules.impl.render;

import net.aetheris.client.modules.Category;
import net.aetheris.client.modules.Module;
import net.aetheris.client.settings.BooleanSetting;
import net.aetheris.client.settings.SliderSetting;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

public class FreeCam extends Module {
    
    private final SliderSetting camSpeed = new SliderSetting("camSpeed", "Speed", "Velocità", 1.0, 0.1, 5.0, 0.1, "x");
    private final BooleanSetting freezeBody = new BooleanSetting("freezeBody", "Freeze Body", "Blocca Corpo", true);

    private RemotePlayer dummyEntity;
    private double startX, startY, startZ;
    private float startYaw, startPitch;

    public FreeCam() {
        super("FreeCam", "Stacca la telecamera dal player.", Category.RENDER);
        addSetting(camSpeed);
        addSetting(freezeBody);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.level == null) {
            toggle();
            return;
        }

        startX = mc.player.getX();
        startY = mc.player.getY();
        startZ = mc.player.getZ();
        startYaw = mc.player.getYRot();
        startPitch = mc.player.getXRot();

        dummyEntity = new RemotePlayer(mc.level, mc.player.getGameProfile());
        dummyEntity.setId(-1337);
        dummyEntity.copyPosition(mc.player);
        dummyEntity.setYHeadRot(mc.player.getYHeadRot());
        dummyEntity.setYBodyRot(mc.player.yBodyRot);
        dummyEntity.setXRot(mc.player.getXRot());
        dummyEntity.setYRot(mc.player.getYRot());
        dummyEntity.xo = startX;
        dummyEntity.yo = startY;
        dummyEntity.zo = startZ;
        
        // Copia l'inventario per renderizzarlo (armatura, oggetti in mano)
        dummyEntity.getInventory().replaceWith(mc.player.getInventory());
        
        mc.level.addEntity(dummyEntity);

        mc.player.noPhysics = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.level == null) return;
        
        if (dummyEntity != null) {
            mc.level.removeEntity(dummyEntity.getId(), Entity.RemovalReason.DISCARDED);
            dummyEntity = null;
        }

        mc.player.setPos(startX, startY, startZ);
        mc.player.noPhysics = false;
        mc.player.setDeltaMovement(0, 0, 0);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        mc.player.noPhysics = true;
        mc.player.getAbilities().flying = true;
        
        // Sincronizza la velocità di volo in base alle impostazioni se necessario
    }
}
