package com.cleanroommc.bogosorter.common.favourite;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Read/write helpers for the favourite tag. Authoritative state is the NBT on the stack itself,
 * so client and server can both interrogate a stack with no extra sync.
 */
public final class FavouriteHelper {

    private FavouriteHelper() {}

    /** True if the stack carries a favourite tag from any player. Cheap; safe on either side. */
    public static boolean isFavourite(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return false;
        return stack.getTagCompound()
            .hasKey(FavouriteNBT.ROOT, 10);
    }

    /** True if the stack is favourited and the owner matches the supplied player. */
    public static boolean isOwnedBy(ItemStack stack, EntityPlayer player) {
        if (player == null) return false;
        String owner = getOwner(stack);
        return owner != null && owner.equals(
            player.getUniqueID()
                .toString());
    }

    /** Owner UUID string, or null if the stack isn't favourited. */
    public static String getOwner(ItemStack stack) {
        if (!isFavourite(stack)) return null;
        NBTTagCompound root = stack.getTagCompound()
            .getCompoundTag(FavouriteNBT.ROOT);
        return root.hasKey(FavouriteNBT.OWNER, 8) ? root.getString(FavouriteNBT.OWNER) : null;
    }

    /** Owner display name at marking time, or null if the stack isn't favourited / has no name. */
    public static String getOwnerName(ItemStack stack) {
        if (!isFavourite(stack)) return null;
        NBTTagCompound root = stack.getTagCompound()
            .getCompoundTag(FavouriteNBT.ROOT);
        return root.hasKey(FavouriteNBT.OWNER_NAME, 8) ? root.getString(FavouriteNBT.OWNER_NAME) : null;
    }

    /**
     * Server-side toggle. Marks/unmarks the stack and returns the new state.
     * Should only be called with a stack pulled directly from the player's inventory,
     * never from a borrowed container slot.
     */
    public static boolean toggle(EntityPlayer player, ItemStack stack) {
        if (player == null || stack == null) return false;
        if (isFavourite(stack)) {
            clear(stack);
            return false;
        }
        mark(player, stack);
        return true;
    }

    private static void mark(EntityPlayer player, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        NBTTagCompound root = tag.hasKey(FavouriteNBT.ROOT, 10) ? tag.getCompoundTag(FavouriteNBT.ROOT)
            : new NBTTagCompound();
        root.setString(
            FavouriteNBT.OWNER,
            player.getUniqueID()
                .toString());
        root.setString(FavouriteNBT.OWNER_NAME, player.getCommandSenderName());
        root.setString(
            FavouriteNBT.TOKEN,
            UUID.randomUUID()
                .toString());
        tag.setTag(FavouriteNBT.ROOT, root);
    }

    private static void clear(ItemStack stack) {
        if (!isFavourite(stack)) return;
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound root = tag.getCompoundTag(FavouriteNBT.ROOT);
        root.removeTag(FavouriteNBT.OWNER);
        root.removeTag(FavouriteNBT.OWNER_NAME);
        root.removeTag(FavouriteNBT.TOKEN);
        if (root.hasNoTags()) tag.removeTag(FavouriteNBT.ROOT);
        if (tag.hasNoTags()) stack.setTagCompound(null);
    }

    /** Convenience for the local client. */
    @SideOnly(Side.CLIENT)
    public static boolean isOwnedByLocalPlayer(ItemStack stack) {
        return isOwnedBy(stack, Minecraft.getMinecraft().thePlayer);
    }
}
