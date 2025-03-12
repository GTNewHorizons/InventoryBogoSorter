package com.cleanroommc.bogosorter.mixins.late.compactstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.tattyseal.compactstorage.inventory.ContainerChestBuilder;
import com.tattyseal.compactstorage.inventory.slot.SlotChestBuilder;
import com.tattyseal.compactstorage.tileentity.TileEntityChestBuilder;

@Mixin(value = ContainerChestBuilder.class, remap = false)
public abstract class MixinContainerChestBuilder extends Container {

    @Shadow
    public int xSize;

    @Shadow
    public TileEntityChestBuilder chest;

    @Shadow
    public EntityPlayer player;

    /**
     * @author 0hwx
     * @reason Register the player slots in the correct order
     */
    @Overwrite
    public void setupSlots() {
        int slotX = this.xSize / 2 - 81 + 1;
        int slotY = 8;

        for (int x = 0; x < 4; ++x) {
            SlotChestBuilder slot = new SlotChestBuilder(chest, x, 7 + x * 18 + 1, 44);
            this.addSlotToContainer(slot);
        }

        slotY = slotY + 108 + 13;

        // Change the layout of the player slots from vertical to horizontal
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot slot = new Slot(player.inventory, x + y * 9 + 9, slotX + (x * 18), slotY + (y * 18));
                super.addSlotToContainer(slot);
            }
        }

        slotY = slotY + 54 + 4;

        for (int x = 0; x < 9; ++x) {
            Slot slot = new Slot(this.player.inventory, x, slotX + x * 18, slotY);
            this.addSlotToContainer(slot);
        }

    }
}
