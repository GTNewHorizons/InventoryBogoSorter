package com.cleanroommc.bogosorter.mixins.early.minecraft;// package com.cleanroommc.bogosorter.core.mixins.early.minecraft;

import com.cleanroommc.bogosorter.common.config.PlayerConfig;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import com.cleanroommc.bogosorter.core.BogoSorterCore;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
 public abstract class MixinEntityPlayer {

    @Shadow
    private ItemStack itemInUse;


    /**
      * Inject custom logic into the `onItemUseFinish` method.
      * We capture the locals, such as `itemstack` and `activeItemStackCopy`, to implement custom logic.
      */
     @Inject(
         method = "onItemUseFinish",
         at = @At(
             value = "INVOKE",
             target = "Lnet/minecraft/entity/player/EntityPlayer;clearItemInUse()V",
             shift = At.Shift.BEFORE
         )
     )
     private void onItemUseFinish(CallbackInfo ci, @Local(name = "itemstack") ItemStack returnedItem) {
         EntityPlayer player = bogosorter$EntityPlayer();
         if (!PlayerConfig.get(player).enableAutoRefill) return;
//         //  used in cases where a modded item returns itself with a different durability (AA coffee, Botania Vials, etc)
//         if (ItemStack.areItemsEqualIgnoreDurability(activeItemStackCopy, itemstack)) {
//             return;
//         }

         if (RefillHandler.shouldHandleRefill(player, itemInUse, true)) {
             boolean didSwap = RefillHandler.handle(player.inventory.currentItem, itemInUse, player, false);
             //new RefillHandler(player.inventory.currentItem, activeItemStackCopy, player).handleRefill();
             if (didSwap && returnedItem != null) {
                 if (!player.inventory.addItemStackToInventory(returnedItem)) {
                     BogoSorterCore.LOGGER.info("Dropping item that does not fit");
                     player.func_146097_a(returnedItem, true, false);
                 }
             }
         }
     }

     private EntityPlayer bogosorter$EntityPlayer() {
         return (EntityPlayer) (Object) this;
     }
 }
