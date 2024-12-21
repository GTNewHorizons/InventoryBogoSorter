package com.cleanroommc.bogosorter.mixins.early.minecraft;// package com.cleanroommc.bogosorter.core.mixins.early.minecraft;

 import com.cleanroommc.bogosorter.common.config.PlayerConfig;
 import com.cleanroommc.bogosorter.common.refill.RefillHandler;
 import com.cleanroommc.bogosorter.core.BogoSorterCore;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import org.spongepowered.asm.mixin.Mixin;
 import org.spongepowered.asm.mixin.injection.At;
 import org.spongepowered.asm.mixin.injection.Inject;
 import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
 import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

 import java.util.Random;

@Mixin(EntityPlayer.class)
 public abstract class MixinEntityPlayer {
    /**
      * Inject custom logic into the `onItemUseFinish` method.
      * We capture the locals, such as `itemstack` and `activeItemStackCopy`, to implement custom logic.
      */
     @Inject(
         method = "onItemUseFinish",
         at = @At(
             value = "INVOKE",
             target = "Lnet/minecraft/entity/player/EntityPlayer;clearItemInUse()V",
             shift = At.Shift.AFTER
         ),
         locals = LocalCapture.CAPTURE_FAILHARD  // Capture the local variables
     )
     private void onItemUseFinish(CallbackInfo ci, int i, ItemStack itemstack) {
         if (!(bogosorter$EntityPlayer() instanceof EntityPlayer)) return;
         EntityPlayer player = (EntityPlayer) bogosorter$EntityPlayer();
         if (!PlayerConfig.get(player).enableAutoRefill) return;
//         //  used in cases where a modded item returns itself with a different durability (AA coffee, Botania Vials, etc)
//         if (ItemStack.areItemsEqualIgnoreDurability(activeItemStackCopy, itemstack)) {
//             return;
//         }

         if (RefillHandler.shouldHandleRefill(player, itemstack, true)) {
             boolean didSwap = RefillHandler.handle(player.inventory.currentItem, itemstack, player, false);
             //new RefillHandler(player.inventory.currentItem, activeItemStackCopy, player).handleRefill();
             if (didSwap && itemstack != null) {
                 if (!player.inventory.addItemStackToInventory(itemstack)) {
                     BogoSorterCore.LOGGER.info("Dropping item that does not fit");
                     player.func_146097_a(itemstack, true, false);
                 }
             }
         }
     }
    @Inject(method = "destroyCurrentEquippedItem",
        at = @At(
        value = "INVOKE",
        target = "net/minecraftforge/event/entity/player/PlayerDestroyItemEvent.<init>(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V", // The target method to inject after
        shift = At.Shift.BY , by = 2
    ),
        locals = LocalCapture.CAPTURE_FAILHARD )
    private void destroyCurrentEquippedItem(CallbackInfo ci, ItemStack orig) {
         System.out.println(orig);
        EntityPlayer player = (EntityPlayer) bogosorter$EntityPlayer();
        RefillHandler.onDestroyItem(player, orig);
    }

     private EntityPlayer bogosorter$EntityPlayer() {
         return (EntityPlayer) (Object) this;
     }
 }
