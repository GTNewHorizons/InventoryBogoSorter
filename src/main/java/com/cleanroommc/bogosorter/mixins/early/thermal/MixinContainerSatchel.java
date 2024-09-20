package com.cleanroommc.bogosorter.mixins.early.thermal;// package com.cleanroommc.bogosorter.core.mixin.early.thermal;
//
// import cofh.thermalexpansion.gui.container.storage.ContainerSatchel;
// import com.cleanroommc.bogosorter.api.ISortableContainer;
// import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
// import net.minecraft.inventory.Container;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
//
// @Mixin(value = ContainerSatchel.class, remap = false)
// public abstract class MixinContainerSatchel implements ISortableContainer {
//
// @Shadow
// int rowSize;
//
// @Shadow
// boolean isCreative;
//
// @Shadow
// boolean isVoid;
//
// @SuppressWarnings("all")
// @Override
// public void buildSortingContext(ISortingContextBuilder builder) {
// if (!isCreative && !isVoid) {
// builder.addSlotGroup(36, ((Container) (Object) this).inventorySlots.size(), rowSize);
// }
// }
// }
