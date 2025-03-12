package com.cleanroommc.bogosorter.mixins.late.compactstorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.tattyseal.compactstorage.api.IChest;
import com.tattyseal.compactstorage.inventory.ContainerChest;
import com.tattyseal.compactstorage.inventory.SlotImmovable;

@Mixin(value = ContainerChest.class, remap = false)
public abstract class MixinContainerChest extends Container implements ISortableContainer {

    @Shadow
    public int xSize;

    @Shadow
    public int invX;

    @Shadow
    public int invY;

    @Shadow
    public IChest chest;

    @Shadow
    public int lastId;

    @Shadow
    public EntityPlayer player;

    @Shadow
    public int backpackSlot;

    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        builder.addSlotGroup(0, invX * invY, invX);
    }

    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return IPosSetter.TOP_RIGHT_HORIZONTAL;
    }

    /**
     * @author 0hwx
     * @reason Register the player slots in the correct order
     */
    @Overwrite
    public void setupSlots() {
        int slotX = this.xSize / 2 - this.invX * 18 / 2 + 1;
        int slotY = 18;
        int lastId = 0;

        for (int y = 0; y < this.invY; ++y) {
            for (int x = 0; x < this.invX; ++x) {
                Slot slot = new Slot(this.chest, lastId, slotX + x * 18, slotY + y * 18);
                super.addSlotToContainer(slot);
                ++lastId;
            }
        }

        this.lastId = lastId;
        slotX = this.xSize / 2 - 81 + 1;
        slotY = slotY + this.invY * 18 + 13;

        // Change the layout of the player slots from vertical to horizontal
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot slot = new Slot(player.inventory, x + y * 9 + 9, slotX + (x * 18), slotY + (y * 18));
                super.addSlotToContainer(slot);
            }
        }

        slotY = slotY + 54 + 4;

        for (int x = 0; x < 9; ++x) {
            boolean immovable = false;
            if (this.backpackSlot != -1 && this.backpackSlot == x) {
                immovable = true;
            }

            SlotImmovable slot = new SlotImmovable(this.player.inventory, x, slotX + x * 18, slotY, immovable);
            super.addSlotToContainer(slot);
        }

    }
}
