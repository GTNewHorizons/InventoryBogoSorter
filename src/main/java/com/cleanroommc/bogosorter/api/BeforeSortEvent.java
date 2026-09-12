package com.cleanroommc.bogosorter.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;

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
 */
@Cancelable
public class BeforeSortEvent extends Event {

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

    /** Container to be sorted. */
    public Container getContainer() {
        return container;
    }

    /** True if a sort keybind triggered this, false for a sort button click or an API call. */
    public boolean isFromKeybind() {
        return fromKeybind;
    }

    static public boolean isSortInGUI() {
        return Minecraft.getMinecraft().currentScreen instanceof GuiContainer;
    }

    static public int getSortKeyCode(final boolean inGUI) {
        return inGUI ? BSKeybinds.sortKeyInGUI.getKeyCode() : BSKeybinds.sortKeyOutsideGUI.getKeyCode();
    }
}
