package net.blay09.mods.craftingtweaks.addon;

import java.util.List;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;
import net.blay09.mods.craftingtweaks.api.DefaultProviderV2;
import net.blay09.mods.craftingtweaks.api.TweakProvider;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GanysDualWorktableTweakProvider implements TweakProvider {

    private final DefaultProviderV2 defaultProvider = CraftingTweaksAPI.createDefaultProviderV2();

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean requiresServerSide() {
        return false;
    }

    @Override
    public int getCraftingGridStart(EntityPlayer entityPlayer, Container container, int id) {
        return id == 0 ? 2 : 11;
    }

    @Override
    public int getCraftingGridSize(EntityPlayer entityPlayer, Container container, int id) {
        return 9;
    }

    @Override
    public void clearGrid(EntityPlayer entityPlayer, Container container, int id, boolean forced) {
        defaultProvider.clearGrid(this, id, entityPlayer, container, false, forced);
    }

    @Override
    public void rotateGrid(EntityPlayer entityPlayer, Container container, int id, boolean counterClockwise) {
        defaultProvider.rotateGrid(this, id, entityPlayer, container, counterClockwise);
    }

    @Override
    public void balanceGrid(EntityPlayer entityPlayer, Container container, int id) {
        defaultProvider.balanceGrid(this, id, entityPlayer, container);
    }

    @Override
    public void spreadGrid(EntityPlayer entityPlayer, Container container, int id) {
        defaultProvider.spreadGrid(this, id, entityPlayer, container);
    }

    @Override
    public boolean canTransferFrom(EntityPlayer entityPlayer, Container container, int id, Slot sourceSlot) {
        return defaultProvider.canTransferFrom(entityPlayer, container, sourceSlot);
    }

    @Override
    public boolean transferIntoGrid(EntityPlayer entityPlayer, Container container, int id, Slot sourceSlot) {
        return defaultProvider.transferIntoGrid(this, id, entityPlayer, container, sourceSlot);
    }

    @Override
    public ItemStack putIntoGrid(EntityPlayer entityPlayer, Container container, int id, ItemStack itemStack,
        int index) {
        return defaultProvider.putIntoGrid(this, id, entityPlayer, container, itemStack, index);
    }

    @Override
    public IInventory getCraftMatrix(EntityPlayer entityPlayer, Container container, int id) {
        return ((Slot) container.inventorySlots.get(getCraftingGridStart(entityPlayer, container, id))).inventory;
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void initGui(GuiContainer guiContainer, List buttonList) {
        // spotless:off
        final int paddingTop = 16;
        final int xSize = ((GuiContainerAccessor) guiContainer).getXSize();
        final int guiLeft = ((GuiContainerAccessor) guiContainer).getGuiLeft();
        final int guiTop = ((GuiContainerAccessor) guiContainer).getGuiTop();
        buttonList.add(CraftingTweaksAPI.createRotateButton(0, guiLeft - 16, guiTop + paddingTop));
        buttonList.add(CraftingTweaksAPI.createBalanceButton(0, guiLeft - 16, guiTop + paddingTop + 18));
        buttonList.add(CraftingTweaksAPI.createClearButton(0, guiLeft - 16, guiTop + paddingTop + 36));
        buttonList.add(CraftingTweaksAPI.createRotateButton(1, guiLeft + xSize, guiTop + paddingTop));
        buttonList.add(CraftingTweaksAPI.createBalanceButton(1, guiLeft + xSize, guiTop + paddingTop + 18));
        buttonList.add(CraftingTweaksAPI.createClearButton(1, guiLeft + xSize, guiTop + paddingTop + 36));
        // spotless:on
    }

    @Override
    public String getModId() {
        return "ganyssurface";
    }
}
