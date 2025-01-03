 package com.cleanroommc.bogosorter.mixins.early.minecraft;

 import com.cleanroommc.bogosorter.api.ISortableContainer;
 import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
 import net.minecraft.entity.passive.EntityHorse;
 import net.minecraft.inventory.ContainerHorseInventory;
 import org.spongepowered.asm.mixin.Final;
 import org.spongepowered.asm.mixin.Mixin;
 import org.spongepowered.asm.mixin.Shadow;

 @Mixin(ContainerHorseInventory.class)
 public class ContainerHorseInventoryMixin implements ISortableContainer {

     @Shadow
     @Final
     private EntityHorse theHorse;

     @Override
     public void buildSortingContext(ISortingContextBuilder builder) {
         if (theHorse.isChested()) {
             builder.addSlotGroup(2, 3, 3);
         }
     }
 }

