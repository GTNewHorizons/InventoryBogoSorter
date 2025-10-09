package com.cleanroommc.bogosorter.common.sort;

import static com.cleanroommc.bogosorter.common.config.BogoSorterConfig.buttonColor;
import static com.cleanroommc.bogosorter.common.config.BogoSorterConfig.buttonEnabled;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.bogosorter.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.utils.Color;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ButtonHandler {

    private static final int SORT_ID = 394658246;
    private static final int SETTINGS_ID = 394658247;
    public static final int BUTTON_SIZE = 10;

    public static final UITexture BUTTON_BACKGROUND = UITexture.builder()
        .location(BogoSorter.ID, "gui/base_button")
        .imageSize(18, 18)
        .adaptable(1)
        .build();

    public static final UITexture BUTTON_SORT = UITexture.builder()
        .location(BogoSorter.ID, "gui/sort")
        .fullImage()
        .build();

    public static final UITexture BUTTON_SETTINGS = UITexture.builder()
        .location(BogoSorter.ID, "gui/settings")
        .fullImage()
        .build();

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (buttonEnabled && ClientEventHandler.isSortableContainer(event.gui)
            && !(event.gui instanceof GuiScreenWrapper)) {
            Container container = ((GuiContainer) event.gui).inventorySlots;
            GuiSortingContext context = GuiSortingContext.getOrCreate(container);
            event.buttonList.removeIf(guiButton -> guiButton instanceof SortButton);
            for (SlotGroup slotGroup : context.getSlotGroups()) {
                if (slotGroup.canBeSorted() && slotGroup.getPosSetter() != null) {
                    event.buttonList.add(new SortButton(slotGroup, true));
                    event.buttonList.add(new SortButton(slotGroup, false));
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (buttonEnabled && ClientEventHandler.isSortableContainer(event.gui)
            && !(event.gui instanceof GuiScreenWrapper)) {
            GuiContainer gui = (GuiContainer) event.gui;
            GuiContainerAccessor guiAccess = (GuiContainerAccessor) gui;
            GuiSortingContext context = GuiSortingContext.getOrCreate(gui.inventorySlots);
            ButtonPos buttonPos = new ButtonPos();
            for (SlotGroup slotGroup : context.getSlotGroups()) {
                if (slotGroup.getPosSetter() == null) continue;
                SortButton sortButton = null, settingsButton = null;
                for (GuiButton guiButton : guiAccess.getButtonList()) {
                    if (guiButton instanceof SortButton button) {
                        if (button.slotGroup == slotGroup) {
                            if (button.sort) {
                                sortButton = button;
                            } else {
                                settingsButton = button;
                            }
                            if (sortButton != null && settingsButton != null) {
                                break;
                            }
                        }
                    }
                }
                if (sortButton == null || settingsButton == null) continue;
                buttonPos.reset();
                slotGroup.getPosSetter()
                    .setButtonPos(slotGroup, buttonPos);
                sortButton.enabled = buttonPos.isEnabled();
                settingsButton.enabled = buttonPos.isEnabled();
                buttonPos.applyPos(guiAccess.getGuiLeft(), guiAccess.getGuiTop(), sortButton, settingsButton);
            }
        }
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (buttonEnabled && ClientEventHandler.isSortableContainer(event.gui)
            && !(event.gui instanceof GuiScreenWrapper)) {
            for (GuiButton guiButton : ((GuiContainerAccessor) event.gui).getButtonList()) {
                if (guiButton instanceof SortButton) {
                    ((SortButton) guiButton).drawTooltip(event.mouseX, event.mouseY);
                }
            }
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
        if (buttonEnabled && event.button instanceof SortButton sortButton && event.button.enabled) {
            if (sortButton.sort) {
                ClientEventHandler.sort(
                    event.gui,
                    sortButton.slotGroup.getSlots()
                        .get(0));
            } else {
                BogoSortAPI.INSTANCE.openConfigGui(event.gui);
                Interactable.playButtonClickSound();
            }
            event.setCanceled(true);
        }
    }

    public static class SortButton extends GuiButton {

        private final SlotGroup slotGroup;
        private final boolean sort;

        public SortButton(SlotGroup slotGroup, boolean sort) {
            super(sort ? SORT_ID : SETTINGS_ID, 0, 0, BUTTON_SIZE, BUTTON_SIZE, sort ? "z" : "...");
            this.slotGroup = slotGroup;
            this.sort = sort;
        }

        @Override
        public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY) {
            if (this.visible && this.enabled) {
                this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
                Color.setGlColor(buttonColor);
                BUTTON_BACKGROUND.draw(this.xPosition, this.yPosition, this.width, this.height);
                Color.resetGlColor();
                this.mouseDragged(mc, mouseX, mouseY);
                int color = 14737632;

                if (packedFGColour != 0) {
                    color = packedFGColour;
                } else if (!this.enabled) {
                    color = 10526880;
                } else if (this.field_146123_n) {
                    color = 16777120;
                }
                Color.setGlColor(color);
                UITexture texture = this.sort ? BUTTON_SORT : BUTTON_SETTINGS;
                texture.draw(this.xPosition, this.yPosition, this.width, this.height);
            }
        }

        public void drawTooltip(int mouseX, int mouseY) {
            if (this.enabled && this.field_146123_n) {
                final List<String> tooltipLines = new ArrayList<>(2);
                if (this.sort) {
                    tooltipLines.add(I18n.format("key.sort_gui"));
                    tooltipLines.add(
                        EnumChatFormatting.DARK_GRAY + I18n.format("key.tooltip.keybind")
                            + " : "
                            + GameSettings.getKeyDisplayString(BSKeybinds.sortKeyInGUI.getKeyCode()));
                } else {
                    tooltipLines.add(I18n.format("key.sort_config"));
                    tooltipLines.add(
                        EnumChatFormatting.DARK_GRAY + I18n.format("key.tooltip.keybind")
                            + " : "
                            + GameSettings.getKeyDisplayString(BSKeybinds.configGuiKey.getKeyCode()));
                }
                if (Minecraft.getMinecraft().currentScreen instanceof GuiScreenAccessor accessor) {
                    accessor.drawHoveringText(tooltipLines, mouseX, mouseY);
                }
            }
        }
    }
}
