package net.blay09.mods.craftingtweaks.addon;

import java.util.List;

import net.blay09.mods.craftingtweaks.api.CraftingTweaksAPI;
import net.blay09.mods.craftingtweaks.api.DefaultProviderV2;
import net.blay09.mods.craftingtweaks.api.RotationHandler;
import net.blay09.mods.craftingtweaks.api.TweakProvider;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TerraFirmaCraftTweakProvider implements TweakProvider {

    private final DefaultProviderV2 defaultProvider = CraftingTweaksAPI.createDefaultProviderV2();

    private final RotationHandler smallRotationHandler = new RotationHandler() {

        @Override
        public boolean ignoreSlotId(int slotId) {
            return false;
        }

        @Override
        public int rotateSlotId(int slotId, boolean counterClockwise) {
            if (!counterClockwise) {
                switch (slotId) {
                    case 0:
                        return 1;
                    case 1:
                        return 3;
                    case 2:
                        return 0;
                    case 3:
                        return 2;
                }
            } else {
                switch (slotId) {
                    case 1:
                        return 0;
                    case 3:
                        return 1;
                    case 0:
                        return 2;
                    case 2:
                        return 3;
                }
            }
            return 0;
        }
    };

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public IInventory getCraftMatrix(EntityPlayer entityPlayer, Container container, int id) {
        return ((ContainerPlayer) container).craftMatrix;
    }

    @Override
    public boolean requiresServerSide() {
        return false;
    }

    @Override
    public int getCraftingGridStart(EntityPlayer entityPlayer, Container container, int id) {
        return 0;
    }

    @Override
    public int getCraftingGridSize(EntityPlayer entityPlayer, Container container, int id) {
        return 4;
    }

    @Override
    public void clearGrid(EntityPlayer entityPlayer, Container container, int id, boolean forced) {
        defaultProvider.clearGrid(this, id, entityPlayer, container, false, forced);
    }

    @Override
    public void rotateGrid(EntityPlayer entityPlayer, Container container, int id, boolean counterClockwise) {
        defaultProvider.rotateGrid(this, id, entityPlayer, container, smallRotationHandler, counterClockwise);
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
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void initGui(GuiContainer guiContainer, List buttonList) {
        // spotless:off
        final int paddingLeft = 1;
        final int paddingTop = -16;
        final int xSize = ((GuiContainerAccessor) guiContainer).getXSize();
        final int guiLeft = ((GuiContainerAccessor) guiContainer).getGuiLeft();
        final int guiTop = ((GuiContainerAccessor) guiContainer).getGuiTop();
        buttonList.add(CraftingTweaksAPI.createRotateButton(0, guiLeft + xSize / 2 + paddingLeft, guiTop + paddingTop));
        buttonList.add(CraftingTweaksAPI.createBalanceButton(0, guiLeft + xSize / 2 + paddingLeft + 18, guiTop + paddingTop));
        buttonList.add(CraftingTweaksAPI.createClearButton(0, guiLeft + xSize / 2 + paddingLeft + 36, guiTop + paddingTop));
        // spotless:on
    }

    @Override
    public String getModId() {
        return "terrafirmacraft";
    }
}
