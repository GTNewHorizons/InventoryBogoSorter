package com.cleanroommc.bogosorter.common.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.dropoff.DropOffButtonHandler;
import com.cleanroommc.bogosorter.common.dropoff.DropOffHandler;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.Theme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ConfigGui extends CustomModularScreen {

    public static boolean wasOpened = false;
    public static final UITexture TOGGLE_BUTTON = UITexture.fullImage("bogosorter:gui/toggle_config", false);
    public static final UITexture ARROW_DOWN_UP = UITexture.fullImage("bogosorter:gui/arrow_down_up", false);
    private static final int DARK_GREY = 0xFF404040;

    public static boolean closeCurrent() {
        ModularScreen screen = ModularScreen.getCurrent();
        if (screen instanceof ConfigGui) {
            screen.close();
            return true;
        }
        return false;
    }

    private final GuiScreen old;
    private Map<SortRule<ItemStack>, AvailableElement> availableElements;
    private Map<NbtSortRule, AvailableElement> availableElementsNbt;

    public ConfigGui(GuiScreen old) {
        super(BogoSorter.ID);
        this.old = old;
    }

    @Override
    public @NotNull ModularPanel buildUI(ModularGuiContext guiContext) {
        this.availableElements = new Object2ObjectOpenHashMap<>();
        this.availableElementsNbt = new Object2ObjectOpenHashMap<>();
        ModularPanel panel = new ModularPanel("bogo_config") {

            @Override
            public boolean shouldAnimate() {
                return super.shouldAnimate() && ConfigGui.this.old == null;
            }

            @Override
            public void onClose() {
                super.onClose();
                Serializer.saveConfig();
                PlayerConfig.syncToServer();
                MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            }
        }.size(300, 250)
            .align(Alignment.Center);

        PagedWidget.Controller controller = new PagedWidget.Controller();

        panel.child(
            new TextWidget(IKey.lang("bogosort.gui.title")).leftRel(0.5f)
                .top(5))
            .child(
                new Rectangle().setColor(DARK_GREY)
                    .asWidget()
                    .left(4)
                    .right(4)
                    .height(1)
                    .top(16))
            .child(
                new PagedWidget<>().controller(controller)
                    .left(4)
                    .right(4)
                    .top(35)
                    .bottom(4)
                    .addPage(createGeneralConfigUI(guiContext))
                    .addPage(createProfilesConfig(guiContext)))
            .child(
                new Row().left(4)
                    .right(4)
                    .height(16)
                    .top(18)
                    .child(
                        new PageButton(0, controller).sizeRel(0.5f, 1f)
                            .disableHoverBackground()
                            .overlay(IKey.lang("bogosort.gui.tab.general.name")))
                    .child(
                        new PageButton(1, controller).sizeRel(0.5f, 1f)
                            .disableHoverBackground()
                            .overlay(IKey.lang("bogosort.gui.tab.profiles.name"))));
        return panel;
    }

    public IWidget createGeneralConfigUI(GuiContext context) {
        Row row = new Row();
        return new ListWidget<>().left(5)
            .right(5)
            .top(2)
            .bottom(2)
            .child(
                new Rectangle().setColor(0xFF606060)
                    .asWidget()
                    .top(1)
                    .left(32)
                    .size(1, 56))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> PlayerConfig.getClient().enableAutoRefill,
                                    val -> PlayerConfig.getClient().enableAutoRefill = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.enable_refill")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()))
            // .childIf(BogoSorter.isQuarkLoaded(), () -> new ColoredIcon(GuiTextures.EXCLAMATION,
            // Color.RED.main).asWidget()
            // .size(14)
            // .tooltip(tooltip -> tooltip.addLine(IKey.lang("bogosort.gui.refill_comment")))))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new TextFieldWidget()
                            .value(
                                new IntValue.Dynamic(
                                    () -> PlayerConfig.getClient().autoRefillDamageThreshold,
                                    val -> PlayerConfig.getClient().autoRefillDamageThreshold = val))
                            .setNumbers(0, Short.MAX_VALUE)
                            .setTextAlignment(Alignment.Center)
                            .setTextColor(IKey.TEXT_COLOR)
                            .background(new Rectangle().setColor(0xFFb1b1b1))
                            .size(30, 14))
                    .child(
                        IKey.lang("bogosort.gui.refill_threshold")
                            .asWidget()
                            .marginLeft(10)
                            .height(14)))
            .child(
                row.widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(new BoolValue.Dynamic(HotbarSwap::isEnabled, HotbarSwap::setEnabled))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .addTooltipLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))
                            .tooltipShowUpTimer(10)
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.hotbar_scrolling")
                            .asWidget()
                            .marginLeft(10)
                            .height(14)
                            .addTooltipLine(IKey.lang("bogosort.gui.hotbar_scrolling.tooltip"))
                            .tooltipShowUpTimer(10)))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> DropOffHandler.enableDropOff,
                                    val -> DropOffHandler.enableDropOff = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.dropoff_enable")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> DropOffButtonHandler.showButton,
                                    val -> DropOffButtonHandler.showButton = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.dropoffbutton_enable")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> DropOffHandler.dropoffRender,
                                    val -> DropOffHandler.dropoffRender = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.dropoff_render")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> DropOffHandler.dropoffChatMessage,
                                    val -> DropOffHandler.dropoffChatMessage = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.dropoff_chatmessage")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()));
    }

    public IWidget createProfilesConfig(GuiContext context) {
        PagedWidget.Controller controller = new PagedWidget.Controller();
        return new ParentWidget<>().widthRel(1f)
            .top(2)
            .bottom(0)
            .child(
                new Rectangle().setColor(DARK_GREY)
                    .asWidget()
                    .top(0)
                    .bottom(4)
                    .width(1)
                    .left(89))
            .child(
                new ListWidget<>() // Profiles
                    .pos(2, 2)
                    .width(81)
                    .bottom(2)
                    .child(
                        new ButtonWidget<>().widthRel(1f)
                            .height(16)
                            .overlay(IKey.str("Profile 1")))
                    .child(
                        IKey.str("Profiles are not yet implemented. They will come in one of the next versions.")
                            .asWidget()
                            .top(20)
                            .width(81)))
            .child(
                new Row().left(92)
                    .right(2)
                    .height(16)
                    .top(2)
                    .child(
                        new PageButton(0, controller).sizeRel(0.5f, 1f)
                            .disableHoverBackground()
                            .overlay(IKey.lang("bogosort.gui.tab.item_sort_rules.name")))
                    .child(
                        new PageButton(1, controller).sizeRel(0.5f, 1f)
                            .disableHoverBackground()
                            .overlay(IKey.lang("bogosort.gui.tab.nbt_sort_rules.name"))))
            .child(
                new PagedWidget<>().controller(controller)
                    .left(90)
                    .right(0)
                    .top(16)
                    .bottom(0)
                    .addPage(createItemSortConfigUI(context))
                    .addPage(createNbtSortConfigUI(context)));
    }

    public IWidget createItemSortConfigUI(GuiContext context) {
        List<SortRule<ItemStack>> allValues = BogoSortAPI.INSTANCE.getItemSortRuleList();
        AtomicReference<SortableListWidget<SortRule<ItemStack>, SortListItem<SortRule<ItemStack>>>> ref = new AtomicReference<>(
            null);
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement().overlay(IKey.lang(value.getNameLangKey()))
                .tooltip(
                    tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey()))
                        .showUpTimer(4))
                .size(80, 14)
                .onMousePressed(mouseButton1 -> {
                    if (this.availableElements.get(value).available) {
                        ref.get()
                            .add(value, -1);
                        this.availableElements.get(value).available = false;
                    }
                    return true;
                });
            this.availableElements.put(value, availableElement);
            return availableElement;
        });
        for (SortRule<ItemStack> value : allValues) {
            this.availableElements.get(value).available = !BogoSorterConfig.sortRules.contains(value);
        }

        SortableListWidget<SortRule<ItemStack>, SortListItem<SortRule<ItemStack>>> sortableListWidget = SortableListWidget
            .sortableBuilder(allValues, BogoSorterConfig.sortRules, s -> {
                TextWidget ruleText = IKey.lang(s.getNameLangKey())
                    .asWidget()
                    .widgetTheme(Theme.BUTTON);
                return new SortListItem<>(
                    s,
                    ruleText.paddingLeft(7)
                        .background(GuiTextures.MC_BUTTON)
                        .tooltip(
                            tooltip -> tooltip.addLine(IKey.lang(s.getDescriptionLangKey()))
                                .showUpTimer(10)));
            });
        ref.set(sortableListWidget);
        return new ParentWidget<>().sizeRel(1f, 1f)
            .child(
                sortableListWidget
                    .onRemove(
                        stringItem -> { this.availableElements.get(stringItem.getWidgetValue()).available = true; })
                    .onChange(list -> {
                        BogoSorterConfig.sortRules.clear();
                        BogoSorterConfig.sortRules.addAll(list);
                    })
                    .left(7)
                    .right(7)
                    .top(7)
                    .bottom(23))
            .child(
                new ButtonWidget<>().bottom(7)
                    .size(12, 12)
                    .leftRel(0.5f)
                    .overlay(GuiTextures.ADD)
                    .onMousePressed(mouseButton -> {
                        if (!isPanelOpen("choose_item_rules")) {
                            ModularPanel panel1 = ModularPanel.defaultPanel("choose_item_rules", 200, 140);
                            isPanelOpen(
                                panel1.child(
                                    new ButtonWidget<>().size(8, 8)
                                        .top(4)
                                        .right(4)
                                        .overlay(GuiTextures.CLOSE)
                                        .onMousePressed(mouseButton1 -> {
                                            panel1.animateClose();
                                            return true;
                                        }))
                                    .child(
                                        new Grid().matrix(availableMatrix)
                                            .scrollable()
                                            .pos(7, 7)
                                            .right(17)
                                            .bottom(7)));
                        }
                        return true;
                    }));
    }

    public IWidget createNbtSortConfigUI(GuiContext context) {
        List<NbtSortRule> allValues = BogoSortAPI.INSTANCE.getNbtSortRuleList();
        AtomicReference<SortableListWidget<NbtSortRule, SortListItem<NbtSortRule>>> ref = new AtomicReference<>(null);
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement().overlay(IKey.lang(value.getNameLangKey()))
                .tooltip(
                    tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey()))
                        .showUpTimer(4))
                .size(80, 14)
                .onMousePressed(mouseButton1 -> {
                    if (this.availableElementsNbt.get(value).available) {
                        ref.get()
                            .add(value, -1);
                        this.availableElementsNbt.get(value).available = false;
                    }
                    return true;
                });
            this.availableElementsNbt.put(value, availableElement);
            return availableElement;
        });
        for (NbtSortRule value : allValues) {
            this.availableElementsNbt.get(value).available = !BogoSorterConfig.nbtSortRules.contains(value);
        }

        SortableListWidget<NbtSortRule, SortListItem<NbtSortRule>> sortableListWidget = SortableListWidget
            .sortableBuilder(allValues, BogoSorterConfig.nbtSortRules, s -> {
                TextWidget ruleText = IKey.lang(s.getNameLangKey())
                    .asWidget()
                    .widgetTheme(Theme.BUTTON);
                return new SortListItem<>(
                    s,
                    ruleText.paddingLeft(7)
                        .background(GuiTextures.MC_BUTTON)
                        .tooltip(
                            tooltip -> tooltip.addLine(IKey.lang(s.getDescriptionLangKey()))
                                .showUpTimer(10)));
            });
        ref.set(sortableListWidget);
        return new ParentWidget<>().sizeRel(1f, 1f)
            .child(
                sortableListWidget
                    .onRemove(
                        stringItem -> { this.availableElementsNbt.get(stringItem.getWidgetValue()).available = true; })
                    .onChange(list -> {
                        BogoSorterConfig.nbtSortRules.clear();
                        BogoSorterConfig.nbtSortRules.addAll(list);
                    })
                    .left(7)
                    .right(7)
                    .top(7)
                    .bottom(23))
            .child(
                new ButtonWidget<>().bottom(7)
                    .size(12, 12)
                    .leftRel(0.5f)
                    .overlay(GuiTextures.ADD)
                    .onMousePressed(mouseButton -> {
                        if (!isPanelOpen("choose_nbt_rules")) {
                            ModularPanel panel1 = ModularPanel.defaultPanel("choose_nbt_rules", 200, 140);
                            isPanelOpen(
                                panel1.child(
                                    new ButtonWidget<>().size(8, 8)
                                        .top(4)
                                        .right(4)
                                        .overlay(GuiTextures.CLOSE)
                                        .onMousePressed(mouseButton1 -> {
                                            panel1.animateClose();
                                            return true;
                                        }))
                                    .child(
                                        new Grid().matrix(availableMatrix)
                                            .scrollable()
                                            .pos(7, 7)
                                            .right(17)
                                            .bottom(7)));
                        }
                        return true;
                    }));
    }

    @Override
    public void onClose() {
        super.onClose();
        Serializer.saveConfig();
        PlayerConfig.syncToServer();
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        wasOpened = false;
        if (this.old != null) {
            // open next tick, otherwise infinite loop
            ClientEventHandler.openNextTick(this.old);
        }
    }

    private static class SortListItem<T extends SortRule<?>> extends SortableListWidget.Item<T> {

        private final IWidget ascendingToggle;

        public SortListItem(T value, IWidget content) {
            super(value, content);
            this.ascendingToggle = new CycleButtonWidget()
                .value(new BoolValue.Dynamic(getWidgetValue()::isInverted, getWidgetValue()::setInverted))
                .stateOverlay(ARROW_DOWN_UP)
                .addTooltip(0, IKey.lang("bogosort.gui.descending"))
                .addTooltip(1, IKey.lang("bogosort.gui.ascending"))
                .heightRel(1f)
                .width(14)
                .pos(0, 0);
            content.flex()
                .left(14)
                .right(10);
            removeable(
                buttonWidget -> buttonWidget.background(GuiTextures.MC_BUTTON)
                    .overlay(
                        GuiTextures.CLOSE.asIcon()
                            .size(8, 8)));
            getChildren().add(this.ascendingToggle);
        }
    }

    private static class AvailableElement extends ButtonWidget<AvailableElement> {

        private boolean available = true;
        private final IDrawable activeBackground = GuiTextures.MC_BUTTON;
        private final IDrawable background = GuiTextures.MC_BUTTON_DISABLED;

        public AvailableElement() {
            disableHoverBackground();
        }

        @Override
        public AvailableElement background(IDrawable... background) {
            throw new UnsupportedOperationException("Use overlay()");
        }

        @Override
        public IDrawable getBackground() {
            return this.available ? activeBackground : background;
        }
    }
}
