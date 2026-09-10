package com.cleanroommc.bogosorter.api;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.jetbrains.annotations.Nullable;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public abstract class BeforeSortEvent extends Event {

    private final EntityPlayer player;
    private final Container container;
    private final @Nullable KeyBinding sortKey;

    public BeforeSortEvent(EntityPlayer player, Container container, @Nullable KeyBinding sortKey) {
        this.player = player;
        this.container = container;
        this.sortKey = sortKey;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public Container getContainer() {
        return container;
    }

    public @Nullable KeyBinding getSortKey() {
        return sortKey;
    }

    public static class BeforeSortInGuiEvent extends BeforeSortEvent {

        public BeforeSortInGuiEvent(EntityPlayer player, Container container, @Nullable KeyBinding sortKey) {
            super(player, container, sortKey);
        }
    }

    public static class BeforeSortOutOfGuiEvent extends BeforeSortEvent {

        public BeforeSortOutOfGuiEvent(EntityPlayer player, Container container, @Nullable KeyBinding sortKey) {
            super(player, container, sortKey);
        }
    }
}
