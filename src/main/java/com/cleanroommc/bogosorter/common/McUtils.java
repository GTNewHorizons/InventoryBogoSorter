package com.cleanroommc.bogosorter.common;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class McUtils {

    public static void dropItem(ItemStack stack, World world, double x, double y, double z) {
        dropItem(stack, world, x, y, z, null, false, 10);
    }

    public static void dropItem(ItemStack stack, World world, double x, double y, double z, @Nullable String thrower,
        boolean noDespawn, int pickUpDelay) {
        if (!world.isRemote && stack != null && !world.restoringBlockSnapshots) {
            double d0 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            double d1 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            double d2 = (double) (world.rand.nextFloat() * 0.5F) + 0.25D;
            EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, stack);
            entityitem.delayBeforeCanPickup = pickUpDelay;
            entityitem.func_145799_b(thrower);
            if (noDespawn) {
                entityitem.age = -6000;
            }
            world.spawnEntityInWorld(entityitem);
        }
    }

    public static void giveItemsToPlayer(EntityPlayer player, List<ItemStack> items) {
        if (player == null || items.isEmpty()) return;
        if (player.worldObj.isRemote) throw new IllegalStateException("Should only be called from server side!");
        PlayerMainInvWrapper itemHandler = new PlayerMainInvWrapper(player.inventory);
        items.removeIf(item -> {
            ItemStack remainder = insertToPlayer(itemHandler, item, false);
            return remainder == null;
        });
        for (ItemStack item : items) {
            player.func_146097_a(item, false, false);
        }
    }

    public static ItemStack insertToPlayer(PlayerMainInvWrapper itemHandler, ItemStack stack, boolean simulate) {
        if (itemHandler == null || stack == null) return stack;

        // not stackable -> just insert into a new slot
        if (!stack.isStackable()) {
            return insertItem(itemHandler, stack, simulate, 9);
        }

        int sizeInventory = itemHandler.getSlots();

        // go through the inventory and try to fill up already existing items
        for (int i = 9; i < sizeInventory; i++) {
            ItemStack slot = itemHandler.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStackRelaxed(slot, stack)) {
                stack = itemHandler.insertItem(i, stack, simulate);

                if (stack == null) {
                    break;
                }
            }
        }

        return insertItem(itemHandler, stack, simulate, 9);
    }

    public static ItemStack insertItem(IItemHandler dest, @Nonnull ItemStack stack, boolean simulate, int startSlot) {
        if (dest == null || stack == null) return stack;

        for (int i = startSlot; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack == null) {
                return null;
            }
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(String sound) {
        SoundHandler soundHandler = Minecraft.getMinecraft()
            .getSoundHandler();
        ResourceLocation resourceLocation = new ResourceLocation(sound);
        PositionedSoundRecord record = PositionedSoundRecord.func_147674_a(resourceLocation, 1.0f);

        soundHandler.playSound(record);
    }

    public static boolean areItemsEqualIgnoreDurability(ItemStack stackA, ItemStack stackB) {
        if (stackA == stackB) {
            return true;
        } else {
            return stackA != null && stackB != null ? isItemEqualIgnoreDurability(stackA, stackB) : false;
        }
    }

    public static boolean isItemEqualIgnoreDurability(ItemStack stackA, ItemStack stackB) {
        if (!stackA.isItemStackDamageable()) {
            return stackA.isItemEqual(stackB);
        } else {
            return stackA != null && stackB != null && stackA.field_151002_e == stackB.field_151002_e;
        }
    }

}
