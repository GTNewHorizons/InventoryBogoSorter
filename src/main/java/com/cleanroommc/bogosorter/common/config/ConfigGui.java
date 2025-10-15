package com.cleanroommc.bogosorter.common.config;

import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.ClientEventHandler;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.client.usageticker.UsageTicker;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.sort.NbtSortRule;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.IThemeApi;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.CustomModularScreen;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.Dialog;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SortableListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ConfigGui extends CustomModularScreen {

    public static boolean wasOpened = false;
    public static final UITexture TOGGLE_BUTTON = UITexture.builder()
        .location("bogosorter:gui/toggle_config")
        .colorType(null)
        .build();
    public static final UITexture ARROW_DOWN_UP = UITexture.builder()
        .location("bogosorter:gui/arrow_down_up")
        .colorType(null)
        .build();
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
    public @NotNull ModularPanel buildUI(ModularGuiContext context) {
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
                MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            }
        }.size(300, 250)
            .align(Alignment.Center);

        PagedWidget.Controller controller = new PagedWidget.Controller();

        panel.child(
            new TextWidget<>(IKey.lang("bogosort.gui.title")).leftRel(0.5f)
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
                    .addPage(createGeneralConfigUI(panel, context))
                    .addPage(createProfilesConfig(panel, context)))
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

    public IWidget createGeneralConfigUI(ModularPanel mainPanel, ModularGuiContext context) {
        Row row = new Row();
        IPanelHandler colorPicker = IPanelHandler.simple(
            mainPanel,
            (parent,
                player) -> new ColorPickerDialog(
                    val -> BogoSorterConfig.buttonColor = val,
                    BogoSorterConfig.buttonColor,
                    true).setDraggable(true),
            true);
        return new ListWidget<>().sizeRel(1)
            .padding(5, 5, 2, 2)
            .child(
                new Rectangle().setColor(0xFF606060)
                    .asWidget()
                    .top(3)
                    .left(32)
                    .size(1, 267))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> BogoSorterConfig.enableHotbarSort,
                                    val -> BogoSorterConfig.enableHotbarSort = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.enable_hotbarSort")
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
                                    () -> BogoSorterConfig.enableAutoRefill,
                                    val -> BogoSorterConfig.enableAutoRefill = val))
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
                                    () -> BogoSorterConfig.autoRefillDamageThreshold,
                                    val -> BogoSorterConfig.autoRefillDamageThreshold = val))
                            .setNumbers(0, Short.MAX_VALUE)
                            .setTextAlignment(Alignment.Center)
                            .setTextColor(IKey.TEXT_COLOR)
                            .background(new Rectangle().setColor(0xFFb1b1b1))
                            .disableHoverBackground()
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
                            .value(
                                new BoolValue.Dynamic(
                                    () -> BogoSorterConfig.enableHotbarSwap,
                                    val -> BogoSorterConfig.enableHotbarSwap = val))
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
                Flow.row()
                    .widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> BogoSorterConfig.buttonEnabled,
                                    val -> BogoSorterConfig.buttonEnabled = val))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .addTooltipLine(IKey.lang("bogosort.gui.button.enabled"))
                            .tooltipShowUpTimer(10)
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.button.enabled")
                            .asWidget()
                            .marginLeft(10)
                            .height(14)
                            .addTooltipLine(IKey.lang("bogosort.gui.button.enabled"))
                            .tooltipShowUpTimer(10)))
            .child(
                Flow.row()
                    .widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new ButtonWidget<>().size(14)
                            .margin(8, 0)
                            .background(((context1, x, y, width, height, widgetTheme) -> {
                                GuiDraw.drawRect(0, 0, 14, 14, 0xFF000000);
                                GuiDraw.drawRect(1, 1, 12, 12, BogoSorterConfig.buttonColor);
                            }))
                            .disableHoverBackground()
                            .onMousePressed(mouseButton -> {
                                colorPicker.openPanel();
                                return true;
                            }))
                    .child(
                        IKey.lang("bogosort.gui.button.color")
                            .asWidget()
                            .marginLeft(10)
                            .height(14)
                            .addTooltipLine(IKey.lang("bogosort.gui.button.color"))
                            .tooltipShowUpTimer(10)))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(
                                new BoolValue.Dynamic(
                                    () -> BogoSorterConfig.dropOff.enableDropOff,
                                    val -> BogoSorterConfig.dropOff.enableDropOff = val))
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
                                    () -> BogoSorterConfig.dropOff.button.showButton,
                                    val -> BogoSorterConfig.dropOff.button.showButton = val))
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
                                    () -> BogoSorterConfig.dropOff.dropoffRender,
                                    val -> BogoSorterConfig.dropOff.dropoffRender = val))
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
                                    () -> BogoSorterConfig.dropOff.dropoffChatMessage,
                                    val -> BogoSorterConfig.dropOff.dropoffChatMessage = val))
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
                            .expanded()))
            .child(
                new Row().widthRel(1f)
                    .height(14)
                    .margin(0, 2)
                    .child(
                        new CycleButtonWidget()
                            .value(new BoolValue.Dynamic(() -> BogoSorterConfig.usageTicker.enableModule, val -> {
                                BogoSorterConfig.usageTicker.enableModule = val;
                                UsageTicker.reloadElements();
                            }))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.usageticker_enable")
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
                            .value(new BoolValue.Dynamic(() -> BogoSorterConfig.usageTicker.enableMainHand, val -> {
                                BogoSorterConfig.usageTicker.enableMainHand = val;
                                UsageTicker.reloadElements();
                            }))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.usageticker_mainhand")
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
                            .value(new BoolValue.Dynamic(() -> BogoSorterConfig.usageTicker.enableOffHand, val -> {
                                BogoSorterConfig.usageTicker.enableOffHand = val;
                                UsageTicker.reloadElements();
                            }))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.usageticker_offhand")
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
                            .value(new BoolValue.Dynamic(() -> BogoSorterConfig.usageTicker.enableArmor, val -> {
                                BogoSorterConfig.usageTicker.enableArmor = val;
                                UsageTicker.reloadElements();
                            }))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.usageticker_armor")
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
                                    () -> BogoSorterConfig.usageTicker.arrow.enableArrow,
                                    val -> { BogoSorterConfig.usageTicker.arrow.enableArrow = val; }))
                            .stateOverlay(TOGGLE_BUTTON)
                            .disableHoverBackground()
                            .size(14, 14)
                            .margin(8, 0)
                            .background(IDrawable.EMPTY))
                    .child(
                        IKey.lang("bogosort.gui.usageticker_arrow")
                            .asWidget()
                            .height(14)
                            .marginLeft(10)
                            .expanded()));

    }

    public IWidget createProfilesConfig(ModularPanel mainPanel, ModularGuiContext context) {
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
                    .addPage(createItemSortConfigUI(mainPanel, context))
                    .addPage(createNbtSortConfigUI(mainPanel, context)));
    }

    private static <T extends SortRule<?>> Map<T, SortableListWidget.Item<T>> getSortListItemMap(Iterable<T> it) {
        final Map<T, SortableListWidget.Item<T>> items = new Object2ObjectOpenHashMap<>();
        for (T sortRule : it) {
            items
                .put(
                    sortRule,
                    new SortableListWidget.Item<>(sortRule).child(
                        item -> new Row()
                            .child(
                                new Widget<>().addTooltipLine(IKey.lang(sortRule.getDescriptionLangKey()))
                                    .widgetTheme(IThemeApi.BUTTON)
                                    // .background(GuiTextures.BUTTON_CLEAN)
                                    .overlay(IKey.lang(sortRule.getNameLangKey()))
                                    .expanded()
                                    .heightRel(1f))
                            .child(
                                new CycleButtonWidget()
                                    .value(new BoolValue.Dynamic(sortRule::isInverted, sortRule::setInverted))
                                    .stateOverlay(ARROW_DOWN_UP)
                                    .addTooltip(0, IKey.lang("bogosort.gui.descending"))
                                    .addTooltip(1, IKey.lang("bogosort.gui.ascending"))
                                    .heightRel(1f)
                                    .width(14))
                            .child(
                                new ButtonWidget<>().onMousePressed(button -> item.removeSelfFromList())
                                    .overlay(
                                        GuiTextures.CROSS_TINY.asIcon()
                                            .size(10))
                                    .width(10)
                                    .heightRel(1f))));
        }
        return items;
    }

    public IWidget createItemSortConfigUI(ModularPanel mainPanel, ModularGuiContext context) {
        List<SortRule<ItemStack>> allValues = BogoSortAPI.INSTANCE.getItemSortRuleList();
        final Map<SortRule<ItemStack>, SortableListWidget.Item<SortRule<ItemStack>>> items = getSortListItemMap(
            allValues);
        SortableListWidget<SortRule<ItemStack>> sortableListWidget = new SortableListWidget<SortRule<ItemStack>>()
            .children(SortRulesConfig.sortRules, items::get)
            .debugName("sortable item list");
        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement().overlay(IKey.lang(value.getNameLangKey()))
                .tooltip(
                    tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey()))
                        .showUpTimer(4))
                .size(80, 14)
                .margin(2, 2, 2, 2)
                .onMousePressed(mouseButton1 -> {
                    if (this.availableElements.get(value).available) {
                        sortableListWidget.child(items.get(value));
                        this.availableElements.get(value).available = false;
                    }
                    return true;
                });
            this.availableElements.put(value, availableElement);
            return availableElement;
        });
        for (SortRule<ItemStack> value : allValues) {
            this.availableElements.get(value).available = !SortRulesConfig.sortRules.contains(value);
        }
        IPanelHandler secPanel = IPanelHandler.simple(mainPanel, (parentPanel, player) -> {
            ModularPanel panel = new Dialog<>("choose_item_rules").setDisablePanelsBelow(true)
                .setDraggable(true)
                .size(200, 140);
            return panel.child(ButtonWidget.panelCloseButton())
                .child(
                    new Grid().matrix(availableMatrix)
                        .scrollable()
                        .pos(7, 7)
                        .right(17)
                        .bottom(7));
        }, true);

        return new ParentWidget<>().sizeRel(1f, 1f)
            .child(
                sortableListWidget
                    .onRemove(
                        stringItem -> { this.availableElements.get(stringItem.getWidgetValue()).available = true; })
                    .onChange(list -> {
                        SortRulesConfig.sortRules.clear();
                        SortRulesConfig.sortRules.addAll(list);
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
                        secPanel.openPanel();
                        return true;
                    }));
    }

    public IWidget createNbtSortConfigUI(ModularPanel mainPanel, ModularGuiContext context) {
        List<NbtSortRule> allValues = BogoSortAPI.INSTANCE.getNbtSortRuleList();
        final Map<NbtSortRule, SortableListWidget.Item<NbtSortRule>> items = getSortListItemMap(allValues);
        SortableListWidget<NbtSortRule> sortableListWidget = new SortableListWidget<NbtSortRule>()
            .children(SortRulesConfig.nbtSortRules, items::get)
            .debugName("sortable nbt list");

        List<List<AvailableElement>> availableMatrix = Grid.mapToMatrix(2, allValues, (index, value) -> {
            AvailableElement availableElement = new AvailableElement().overlay(IKey.lang(value.getNameLangKey()))
                .tooltip(
                    tooltip -> tooltip.addLine(IKey.lang(value.getDescriptionLangKey()))
                        .showUpTimer(4))
                .size(80, 14)
                .margin(2, 2, 2, 2)
                .onMousePressed(mouseButton1 -> {
                    if (this.availableElementsNbt.get(value).available) {
                        sortableListWidget.child(items.get(value));
                        this.availableElementsNbt.get(value).available = false;
                    }
                    return true;
                });
            this.availableElementsNbt.put(value, availableElement);
            return availableElement;
        });
        for (NbtSortRule value : allValues) {
            this.availableElementsNbt.get(value).available = !SortRulesConfig.nbtSortRules.contains(value);
        }

        IPanelHandler secPanel = IPanelHandler.simple(mainPanel, (parentPanel, player) -> {
            ModularPanel panel = new Dialog<>("choose_nbt_rules").setDisablePanelsBelow(true)
                .setDraggable(true)
                .size(200, 140);
            return panel.child(ButtonWidget.panelCloseButton())
                .child(
                    new Grid().matrix(availableMatrix)
                        .scrollable()
                        .pos(7, 7)
                        .right(17)
                        .bottom(7));
        }, true);
        return new ParentWidget<>().sizeRel(1f, 1f)
            .child(
                sortableListWidget
                    .onRemove(
                        stringItem -> { this.availableElementsNbt.get(stringItem.getWidgetValue()).available = true; })
                    .onChange(list -> {
                        SortRulesConfig.nbtSortRules.clear();
                        SortRulesConfig.nbtSortRules.addAll(list);
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
                        secPanel.openPanel();
                        return true;
                    }));
    }

    @Override
    public void onClose() {
        super.onClose();
        saveForgeConfig();
        Serializer.saveConfig();
        MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
        wasOpened = false;
        if (this.old != null) {
            // open next tick, otherwise infinite loop
            ClientEventHandler.openNextTick(this.old);
        }
    }

    public static void saveForgeConfig() {
        ConfigurationManager.save(BogoSorterConfig.class);
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
