package com.cleanroommc.bogosorter.common.favourite;

/** NBT keys used to mark a stack as a player's favourite. */
public final class FavouriteNBT {

    private FavouriteNBT() {}

    /** Compound tag attached to {@link net.minecraft.item.ItemStack#stackTagCompound}. */
    public static final String ROOT = "BogoFav";

    /** UUID of the player who marked the stack. */
    public static final String OWNER = "Owner";

    /** Display name of the player who marked the stack at the time of marking. */
    public static final String OWNER_NAME = "OwnerName";

    /** Random per-mark token so stacks split off a favourite don't accidentally inherit pinning. */
    public static final String TOKEN = "Token";
}
