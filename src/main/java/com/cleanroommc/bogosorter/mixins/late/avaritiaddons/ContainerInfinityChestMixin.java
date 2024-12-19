package com.cleanroommc.bogosorter.mixins.late.avaritiaddons;//package com.cleanroommc.bogosorter.mixins.early.avaritiaddons;
//
// import com.cleanroommc.bogosorter.BogoSortAPI;
// import com.cleanroommc.bogosorter.api.ISlot;
// import com.cleanroommc.modularui.api.inventory.ClickType;
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.inventory.Container;
// import net.minecraft.inventory.Slot;
// import net.minecraft.item.ItemStack;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// import wanion.avaritiaddons.block.chest.infinity.ContainerInfinityChest;
//
// import javax.annotation.Nonnull;
//
// @Mixin(ContainerInfinityChest.class)
// public abstract class ContainerInfinityChestMixin extends Container {
//
//     @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
//     public void onSlotClick(int slotId, final int mouseButton, final int modifier, final EntityPlayer entityPlayer,
//                             CallbackInfoReturnable<ItemStack> cir) {
//         if (slotId < 0) return;
//         Container container = (Container) (Object) this;
//         ISlot slot = BogoSortAPI.getSlot(container, slotId);
//         if (slot != null) {
//             transferStackInSlot(entityPlayer, slotId);
//             cir.setReturnValue(null);
//         }
//     }
// }
