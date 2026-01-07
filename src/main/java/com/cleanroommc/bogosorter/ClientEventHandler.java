package com.cleanroommc.bogosorter;

import static com.cleanroommc.bogosorter.ShortcutHandler.SetCanTakeStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.client.keybinds.KeyBind;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.config.SortRulesConfig;
import com.cleanroommc.bogosorter.common.dropoff.render.RendererCube;
import com.cleanroommc.bogosorter.common.network.CDropOff;
import com.cleanroommc.bogosorter.common.network.CSort;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.compat.screen.WarningScreen;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class ClientEventHandler {

    public static final List<ItemStack> allItems = new ArrayList<>();
    public static final boolean isDeobfuscatedEnvironment = (boolean) Launch.blackboard
        .get("fml.deobfuscatedEnvironment");

    private static long timeConfigGui = 0;
    private static long timeSort = 0;
    private static long timeShortcut = 0;
    private static long timeDropoff = 0;
    private static long ticks = 0;

    private static GuiScreen nextGui = null;

    public static void openNextTick(GuiScreen screen) {
        ClientEventHandler.nextGui = screen;
    }

    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ticks++;
        }
        if (ClientEventHandler.nextGui != null) {
            ClientGUI.open(ClientEventHandler.nextGui);
            ClientEventHandler.nextGui = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && !WarningScreen.wasOpened) {
            WarningScreen.wasOpened = true;
            List<String> warnings = new ArrayList<>();
            if (Loader.isModLoaded("inventorytweaks")) {
                warnings.add("InventoryTweaks is loaded. This will cause issues!");
                warnings.add("Consider removing the mod and reload the game.");
            }
            if (!warnings.isEmpty()) {
                warnings.add(0, EnumChatFormatting.BOLD + "! Warning from Inventory Bogosorter !");
                warnings.add(1, "");
                event.gui = new WarningScreen(warnings);
            }
        }
    }

    private static void shortcutAction() {
        timeShortcut = Minecraft.getSystemTime();
    }

    private static boolean canDoShortcutAction() {
        return Minecraft.getSystemTime() - timeShortcut > 50;
    }

    // i have to subscribe to 4 events to catch all inputs

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.MouseInputEvent event) {
        handleInput(null);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiKeyInput(KeyboardInputEvent.Pre event) {
        KeyBind.checkKeys(getTicks());
        if (!(event.gui instanceof GuiContainer)) return;
        if (handleInput((GuiContainer) event.gui)) {
            event.setCanceled(true);
            return;
        }

        if (isDeobfuscatedEnvironment) {
            // clear
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)) {
                SlotAccessor slot = getSlot(event.gui);
                SortHandler sortHandler = createSortHandler(event.gui, slot);
                if (sortHandler == null) return;
                sortHandler.clearAllItems(slot);
                return;
            }
            // random
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
                if (allItems.isEmpty()) {
                    for (Object key : Item.itemRegistry.getKeys()) { // Iterate over the registry keys
                        Item item = (Item) Item.itemRegistry.getObject(key); // Get the actual Item using the key
                        List<ItemStack> subItems = Lists.newArrayList();
                        item.getSubItems(item, CreativeTabs.tabAllSearch, subItems); // Get sub-items based on the
                        // creative tab
                        allItems.addAll(subItems);
                    }
                }
                SlotAccessor slot = getSlot(event.gui);
                SortHandler sortHandler = createSortHandler(event.gui, slot);
                if (sortHandler == null) return;
                sortHandler.randomizeItems(slot);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onMouseInput(MouseInputEvent.Pre event) {
        KeyBind.checkKeys(getTicks());
        if (event.gui instanceof GuiContainer && handleInput((GuiContainer) event.gui)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        if (BogoSorterConfig.dropOff.dropoffRender) {
            RendererCube.INSTANCE.tryToRender(event);
        }
    }

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {

        if (container != null && canDoShortcutAction()) {
            KeyBind key;

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_ALL);
            if (key != null && key.isFirstPress() && ShortcutHandler.moveAllItems(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_ALL_SAME);
            if (key != null && key.isFirstPress() && ShortcutHandler.moveAllItems(container, true)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_SINGLE);
            if (key != null && key.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.MOVE_SINGLE_EMPTY);
            if (key != null && key.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, true)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.THROW_ALL);
            if (key != null && key.isFirstPress() && ShortcutHandler.dropItems(container, false)) {
                shortcutAction();
                return true;
            }

            key = BSKeybinds.getActiveKeyBind(BSKeybinds.THROW_ALL_SAME);
            if (key != null && key.isFirstPress() && ShortcutHandler.dropItems(container, true)) {
                shortcutAction();
                return true;
            }
            SetCanTakeStack = true;
        }
        if (Keypress(BSKeybinds.sortKeyOutsideGUI)) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                sort(Minecraft.getMinecraft().thePlayer.inventoryContainer, null, 9); // main inventory
                sort(Minecraft.getMinecraft().thePlayer.inventoryContainer, null, 36); // hotbar

                timeSort = t;
                return true;
            }
        }
        if (Keypress(BSKeybinds.sortKeyInGUI)) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                if (container != null) {
                    SlotAccessor slot = getSlot(container);
                    if (!canSort(slot) || !sort(container, slot)) {
                        return false;
                    }
                    timeSort = t;
                    return true;
                }
            }
        }
        if (Keypress(BSKeybinds.configGuiKey)) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (!ConfigGui.closeCurrent()) {
                    BogoSortAPI.INSTANCE.openConfigGui(Minecraft.getMinecraft().currentScreen);
                }
                timeConfigGui = t;
            }
        }
        if (Keypress(BSKeybinds.dropoffKey)) {
            long t = Minecraft.getSystemTime();
            if (t - timeDropoff > BogoSorterConfig.dropOff.dropoffPacketThrottleInMS) {
                if (BogoSorterConfig.dropOff.enableDropOff) {
                    NetworkHandler.sendToServer(new CDropOff());
                }
                timeDropoff = t;
            }
        }
        return false;
    }

    private static boolean canSort(@Nullable SlotAccessor slot) {
        return !Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode
            || (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null
                && (slot == null || slot.callGetStack() == null));
    }

    private static boolean isButtonPressed(int button) {
        return Mouse.getEventButtonState() && Mouse.getEventButton() == button;
    }

    private static boolean isKeyDown(KeyBinding key) {
        if (key.getKeyCode() == 0) return false;

        if (key.getKeyCode() < 0) {
            return isButtonPressed(key.getKeyCode() + 100);
        }
        return Keyboard.getEventKeyState() && Keyboard.getEventKey() == key.getKeyCode();
    }

    public static boolean isSortableContainer(GuiScreen screen) {
        return screen instanceof GuiContainer && BogoSortAPI.isValidSortable(((GuiContainer) screen).inventorySlots);
    }

    public static <T extends Container & ISortableContainer> T getSortableContainer(GuiScreen screen) {
        return (T) ((GuiContainer) screen).inventorySlots;
    }

    @Nullable
    public static SlotAccessor getSlot(GuiScreen guiScreen) {
        if (guiScreen instanceof GuiContainer) {
            return (SlotAccessor) ((GuiContainer) guiScreen).theSlot;
        }
        return null;
    }

    public static boolean sort(GuiScreen guiScreen, @Nullable SlotAccessor slot) {
        if (guiScreen instanceof GuiContainer) {
            return sort(((GuiContainer) guiScreen).inventorySlots, slot, -1);
        }
        return false;
    }

    public static boolean sort(Container container, @Nullable SlotAccessor slot, int slotNumber) {
        GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);
        if (sortingContext.isEmpty()) return false;
        SlotGroup slotGroup = null;
        if (slot == null && slotNumber == -1) {
            if (sortingContext.getNonPlayerSlotGroupAmount() == 1) {
                slotGroup = sortingContext.getNonPlayerSlotGroup();
            } else if (sortingContext.hasPlayer() && sortingContext.getNonPlayerSlotGroupAmount() == 0) {
                slotGroup = sortingContext.getPlayerSlotGroup();
            }
            if (slotGroup == null || slotGroup.isEmpty()) return false;
            slot = slotGroup.getSlots()
                .get(0);
        } else {
            slotGroup = sortingContext.getSlotGroup(slot != null ? slot.getSlotNumber() : slotNumber);
            if (slotGroup == null || slotGroup.isEmpty()
                || (slotGroup.isHotbar() && !BogoSorterConfig.enableHotbarSort)) return false;
        }

        List<SortRule<ItemStack>> sortRules = SortRulesConfig.sortRules;
        boolean color = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));
        boolean name = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
        NetworkHandler.sendToServer(
            new CSort(
                createSortData(slotGroup, color, name),
                SortRulesConfig.sortRules,
                SortRulesConfig.nbtSortRules,
                slot != null ? slot.getSlotNumber() : slotNumber,
                slotGroup.isPlayerInventory()));
        SortHandler.playSortSound();

        return true;

    }

    public static Collection<ClientSortData> createSortData(SlotGroup slotGroup, boolean color, boolean name) {
        if (!color && !name) return Collections.emptyList();
        Map<ItemStack, ClientSortData> map = new Object2ObjectOpenCustomHashMap<>(
            BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (SlotAccessor slot1 : slotGroup.getSlots()) {
            map.computeIfAbsent(slot1.callGetStack(), stack -> ClientSortData.of(stack, color, name))
                .getSlotNumbers()
                .add(slot1.getSlotNumber());
        }
        return map.values();
    }

    public static SortHandler createSortHandler(GuiScreen guiScreen, @Nullable SlotAccessor slot) {
        if (slot != null && guiScreen instanceof GuiContainer) {

            Container container = ((GuiContainer) guiScreen).inventorySlots;
            boolean player = BogoSortAPI.isPlayerSlot(slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(Minecraft.getMinecraft().thePlayer, container, Int2ObjectMaps.emptyMap());
        }
        return null;
    }

    private static boolean Keypress(KeyBinding key) {
        int keyCode = key.getKeyCode();
        if (keyCode == 0) return false;
        if (keyCode > 0) {
            return key.isPressed() || Keyboard.isKeyDown(keyCode);
        } else {
            return Mouse.isButtonDown(100 + keyCode);
        }
    }
}
