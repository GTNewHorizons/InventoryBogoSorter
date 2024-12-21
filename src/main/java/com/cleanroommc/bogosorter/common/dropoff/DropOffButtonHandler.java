package com.cleanroommc.bogosorter.common.dropoff;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;
import com.cleanroommc.bogosorter.common.sort.IGuiContainerAccessor;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraftforge.client.event.GuiScreenEvent;

public class DropOffButtonHandler {

    public static int buttonX = 80;
    public static int buttonY = 12;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen screen = event.gui;
        if (screen instanceof GuiContainerCreative) {
            return;
        }
        try {
            if (screen instanceof InventoryEffectRenderer inv) {
                event.buttonList.add(new InvButton(inv));
            }
        } catch (NullPointerException e) {
            BogoSorter.LOGGER.error(
                "Erroring adding dropoff button to player inventory \n" + e);
        }
    }


    @SubscribeEvent
    public  void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiScreen screen = event.gui;

        if (screen instanceof InventoryEffectRenderer) {
            for (GuiButton guiButton : ((IGuiContainerAccessor) event.gui).getButtons()) {
                if (guiButton instanceof InvButton invButton) {
                    invButton.drawTooltip(event.mouseX, event.mouseY);
                }
            }
        }
    }
}
