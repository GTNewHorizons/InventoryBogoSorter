package com.cleanroommc.bogosorter.mixins.early.avaritiaddons;// package com.cleanroommc.bogosorter.core.mixin.early.avaritiaddons;
//
// import com.cleanroommc.bogosorter.BogoSortAPI;
// import com.cleanroommc.bogosorter.api.ISlot;
// import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
// import com.cleanroommc.bogosorter.common.sort.SlotGroup;
// import com.cleanroommc.bogosorter.compat.InfinitySlotWrapper;
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.inventory.ClickType;
// import net.minecraft.inventory.Container;
// import net.minecraft.item.ItemStack;
// import org.jetbrains.annotations.NotNull;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
// import wanion.avaritiaddons.block.chest.infinity.ContainerInfinityChest;
//
// import javax.annotation.Nonnull;
//
// @Mixin(ContainerInfinityChest.class)
// public abstract class ContainerInfinityChestMixin {
//
// @Shadow
// @Nonnull
// public abstract ItemStack transferStackInSlot(@NotNull EntityPlayer entityPlayer, int slot);
//
// @Inject(method = "slotClick", at = @At("HEAD"), cancellable = true)
// public void onSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player,
// CallbackInfoReturnable<ItemStack> cir) {
// if (slotId < 0) return;
// Container container = (Container) (Object) this;
// ISlot slot = BogoSortAPI.getSlot(container, slotId);
// if (slot instanceof InfinitySlotWrapper && clickTypeIn == ClickType.QUICK_MOVE) {
// transferStackInSlot(player, slotId);
// cir.setReturnValue(null);
// }
// }
//
// @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true)
// public void transferItem(EntityPlayer entityPlayer, int slotNumber, CallbackInfoReturnable<ItemStack> cir) {
// Container container = (Container) (Object) this;
// ISlot slot = BogoSortAPI.getSlot(container, slotNumber);
//
// if (slot.bogo$getStack().isEmpty()) {
// cir.setReturnValue(null);
// return;
// }
// ItemStack stack = slot.bogo$getStack();
// ItemStack toInsert = stack.copy();
// int amount = Math.min(stack.getCount(), stack.getMaxStackSize());
// toInsert.setCount(amount);
// GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);
//
// SlotGroup slots = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
// SlotGroup otherSlots = BogoSortAPI.isPlayerSlot(slot) ? sortingContext.getNonPlayerSlotGroup() :
// sortingContext.getPlayerSlotGroup();
// if (otherSlots == null || slots == otherSlots) {
// cir.setReturnValue(null);
// return;
// }
//
// toInsert = BogoSortAPI.insert(container, otherSlots.getSlots(), toInsert);
// stack.shrink(amount - toInsert.getCount());
// slot.bogo$putStack(stack.isEmpty() ? null : stack);
//
// cir.setReturnValue(toInsert);
// }
// }
