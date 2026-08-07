package net.aetheris.client.mixins;

import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceMenu.class)
public interface AbstractFurnaceMenuAccessor {
    @Invoker("isFuel")
    boolean invokeIsFuel(ItemStack stack);
}
