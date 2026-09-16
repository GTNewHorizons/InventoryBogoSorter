package com.cleanroommc.bogosorter.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired on the client before a sort is performed. Cancel to suppress the sort and let the triggering input fall
 * through to other handlers.
 * <p>
 * {@link #getSortKeyCode()} returns the key that triggered the sort, or {@link #NO_KEY} for a sort button click or an
 * {@link IBogoSortAPI#sortSlotGroup} call. A handler that only wants to reclaim its own keybind can compare against
 * its key code and will therefore not suppress deliberate sorts.
 * <p>
 */
@Cancelable
public class BeforeSortEvent extends Event {

    /**
     * Value of {@link #getSortKeyCode()} when the sort was not triggered by a key press (same as
     * {@code Keyboard.KEY_NONE}).
     */
    public static final int NO_KEY = 0;

    private final EntityPlayer player;
    private final Container container;
    private final boolean inGui;
    private final int sortKeyCode;

    public BeforeSortEvent(EntityPlayer player, Container container, boolean inGui, int sortKeyCode) {
        this.player = player;
        this.container = container;
        this.inGui = inGui;
        this.sortKeyCode = sortKeyCode;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    /** Container to be sorted. */
    public Container getContainer() {
        return container;
    }

    /** True if the sort happens while a GUI is open. */
    public boolean isInGui() {
        return inGui;
    }

    /** Key code of the key press that triggered this sort, or {@link #NO_KEY} if it was not triggered by a key. */
    public int getSortKeyCode() {
        return sortKeyCode;
    }

    /** True if a sort keybind triggered this, false for a sort button click or an API call. */
    public boolean isFromKeybind() {
        return sortKeyCode != NO_KEY;
    }

}
