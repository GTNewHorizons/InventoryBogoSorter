package com.cleanroommc.bogosorter.mixins.early.thermal;// package com.cleanroommc.bogosorter.core.mixin.early.thermal;
//
// import cofh.thermalexpansion.gui.container.storage.ContainerStrongbox;
// import com.cleanroommc.bogosorter.api.ISortableContainer;
// import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
// import net.minecraft.inventory.Container;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
//
// @Mixin(value = ContainerStrongbox.class, remap = false)
// public abstract class MixinContainerStrongbox implements ISortableContainer {
//
// @Shadow
// int rowSize;
//
// @Shadow
// int storageIndex;
//
// @SuppressWarnings("all")
// @Override
// public void buildSortingContext(ISortingContextBuilder builder) {
// if (storageIndex != 0) {
// builder.addSlotGroup(36, ((Container) (Object) this).inventorySlots.size(), rowSize);
// }
// }
// }
