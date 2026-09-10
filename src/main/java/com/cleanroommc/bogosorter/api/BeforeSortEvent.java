package com.cleanroommc.bogosorter.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Fired on the client before a sort is performed. Cancel to suppress the sort and let the triggering input fall
 * through to other handlers.
 * <p>
 * {@link #isFromKeybind()} distinguishes a sort key press from a sort button click or an
 * {@link IBogoSortAPI#sortSlotGroup} call, so a handler that only wants to reclaim its keybind does not suppress
 * deliberate sorts.
 * <p>
 * The out of GUI variant is fired once per slot group (main inventory and hotbar), so a handler that wants to block
 * the whole sort must cancel both.
 */
@Cancelable
public abstract class BeforeSortEvent extends Event {

    private final EntityPlayer player;
    private final Container container;
    private final boolean fromKeybind;

    public BeforeSortEvent(EntityPlayer player, Container container, boolean fromKeybind) {
        this.player = player;
        this.container = container;
        this.fromKeybind = fromKeybind;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public Container getContainer() {
        return container;
    }

    /** True if a sort keybind triggered this, false for a sort button click or an API call. */
    public boolean isFromKeybind() {
        return fromKeybind;
    }

    /** Fired while a container GUI is open. */
    public static class BeforeSortInGuiEvent extends BeforeSortEvent {

        public BeforeSortInGuiEvent(EntityPlayer player, Container container, boolean fromKeybind) {
            super(player, container, fromKeybind);
        }
    }

    /** Fired while no container GUI is open. */
    public static class BeforeSortOutOfGuiEvent extends BeforeSortEvent {

        public BeforeSortOutOfGuiEvent(EntityPlayer player, Container container, boolean fromKeybind) {
            super(player, container, fromKeybind);
        }
    }
}
