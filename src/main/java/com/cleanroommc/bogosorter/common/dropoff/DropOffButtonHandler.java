package com.cleanroommc.bogosorter.common.dropoff;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DropOffButtonHandler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!BogoSorterConfig.dropOff.enableDropOff) return;
        GuiScreen screen = event.gui;
        if (!BogoSorterConfig.dropOff.button.showButton || screen instanceof GuiContainerCreative) {
            return;
        }
        try {
            if (screen instanceof GuiInventory inv) {
                event.buttonList.add(new DropOffInvButton(inv));
            }
        } catch (NullPointerException e) {
            BogoSorter.LOGGER.error("Erroring adding dropoff button to player inventory \n" + e);
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!BogoSorterConfig.dropOff.enableDropOff) return;
        GuiScreen screen = event.gui;
        if (!BogoSorterConfig.dropOff.button.showButton) return;
        if (screen instanceof GuiInventory) {
            for (GuiButton guiButton : ((GuiContainerAccessor) event.gui).getButtonList()) {
                if (guiButton instanceof DropOffInvButton invButton) {
                    invButton.drawTooltip(event.mouseX, event.mouseY);
                }
            }
        }
    }
}
