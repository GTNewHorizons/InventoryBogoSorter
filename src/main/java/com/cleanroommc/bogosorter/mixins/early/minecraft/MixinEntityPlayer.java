package com.cleanroommc.bogosorter.mixins.early.minecraft;// package com.cleanroommc.bogosorter.core.mixins.early.minecraft;

 import com.cleanroommc.bogosorter.common.config.PlayerConfig;
 import com.cleanroommc.bogosorter.common.refill.RefillHandler;
 import com.cleanroommc.bogosorter.core.BogoSorterCore;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.event.ForgeEventFactory;
 import org.spongepowered.asm.mixin.Mixin;
 import org.spongepowered.asm.mixin.Shadow;
 import org.spongepowered.asm.mixin.injection.At;
 import org.spongepowered.asm.mixin.injection.Inject;
 import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
 import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

 @Mixin(EntityPlayer.class)
 public abstract class MixinEntityPlayer {
     @Shadow
     private ItemStack itemInUse;

     @Shadow
     private int itemInUseCount;

     /**
      * Inject custom logic into the `onItemUseFinish` method.
      * We capture the locals, such as `itemstack` and `activeItemStackCopy`, to implement custom logic.
      */
     @Inject(
         method = "onItemUseFinish",  // Target the `onItemUseFinish` method
         at = @At(
             value = "INVOKE",  // Look for an INVOKE instruction
             target = "Lnet/minecraft/entity/player/EntityPlayer;clearItemInUse()V", // The target method to inject after
             shift = At.Shift.AFTER  // Inject after the target method
         ),
         locals = LocalCapture.CAPTURE_FAILHARD  // Capture the local variables
     )
     private void onItemUseFinish(CallbackInfo ci) {
         // Get the current player instance
         EntityPlayer player = (EntityPlayer) (Object) this;

         // Ensure that auto-refill is enabled for this player (this could be a config setting)
         if (!PlayerConfig.get(player).enableAutoRefill) return;

         // Capture the locals from the `onItemUseFinish` method in `EntityPlayer`
         ItemStack itemstack = this.itemInUse.onFoodEaten(null, player);
         itemstack = ForgeEventFactory.onItemUseFinish(player, this.itemInUse, this.itemInUseCount, itemstack);

         // You need to ensure that itemInUse and itemstack are set to appropriate values from the method
         // Here, we assume they are captured correctly by the Mixin and set during the execution

         // Check if the item used is the same as the returned itemstack (e.g., no change in item)
         if (ItemStack.areItemStacksEqual(this.itemInUse, itemstack)) {
             return;
         }

         // Handle refill logic (if necessary, based on your own refill handler logic)
         if (RefillHandler.shouldHandleRefill(player, itemInUse, true)) {
             boolean didSwap = RefillHandler.handle(player.inventory.currentItem, itemInUse, player, false);

             // Perform custom refill logic
             new RefillHandler(player.inventory.currentItem, itemInUse, player).handleRefill();

             // If an item was swapped and is non-null, attempt to add it to the player's inventory
             if (didSwap && itemstack != null) {
                 if (!player.inventory.addItemStackToInventory(itemstack)) {
                     BogoSorterCore.LOGGER.info("Dropping item that does not fit");
                     // Drop the item if it doesn't fit in the inventory
                     player.func_146097_a(itemstack, true, false);
                 }
             }
         }
     }
 }
