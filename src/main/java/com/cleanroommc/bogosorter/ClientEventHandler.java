package com.cleanroommc.bogosorter;

import com.cleanroommc.bogosorter.api.ISlot;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.SortRule;
import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.config.ConfigGui;
import com.cleanroommc.bogosorter.common.network.CSort;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.sort.ClientSortData;
import com.cleanroommc.bogosorter.common.sort.GuiSortingContext;
import com.cleanroommc.bogosorter.common.sort.SlotGroup;
import com.cleanroommc.bogosorter.common.sort.SortHandler;
import com.cleanroommc.bogosorter.compat.screen.WarningScreen;
import com.cleanroommc.modularui.api.event.KeyboardInputEvent;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.event.GuiOpenEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ClientEventHandler {

    public ClientEventHandler() {}

    public static final List<ItemStack> allItems = new ArrayList<>();
    public static final KeyBinding configGuiKey = new KeyBinding("key.sort_config", Keyboard.KEY_K, "key.categories.bogosorter");
    public static final KeyBinding sortKey = new KeyBinding("key.sort", -98, "key.categories.bogosorter");
    public static final boolean isDeobfuscatedEnvironment = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    public static final KeyBind moveAllSame = KeyBind.builder("move_all_same")
            .lmb(true).rmb(false)
            .shift(false).ctrl(false).alt(true).space(false)
            .build();
    public static final KeyBind moveAll = KeyBind.builder("move_all")
            .lmb(true).rmb(false)
            .shift(false).ctrl(false).alt(false).space(true)
            .build();
    public static final KeyBind moveSingle = KeyBind.builder("move_single")
            .lmb(true).rmb(false)
            .shift(false).ctrl(true).alt(false)
            .build();
    public static final KeyBind moveSingleEmpty = KeyBind.builder("move_single_empty")
            .lmb(false).rmb(true)
            .shift(false).ctrl(true).alt(false)
            .build();
    public static final KeyBind throwAllSame = KeyBind.builder("throw_all_same")
            .lmb(false).rmb(false)
            .shift(false).ctrl(false).alt(true).space(false)
            .validator(() -> isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop))
            .build();
    public static final KeyBind throwAll = KeyBind.builder("throw_all")
            .lmb(false).rmb(false)
            .shift(false).ctrl(false).alt(false).space(true)
            .validator(() -> isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindDrop))
            .build();

    private static long timeConfigGui = 0;
    private static long timeSort = 0;
    private static long timeShortcut = 0;
    private static long ticks = 0;

    private static GuiScreen nextGui = null;

    public static void openNextTick(GuiScreen screen) {
        ClientEventHandler.nextGui = screen;
    }

    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
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
                warnings.add(0, ChatFormatting.BOLD + "! Warning from Inventory Bogosorter !");
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
                ISlot slot = getSlot(event.gui);
                SortHandler sortHandler = createSortHandler(event.gui, slot);
                if (sortHandler == null) return;
                sortHandler.clearAllItems(slot);
                return;
            }
            // random
            if (Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)) {
//                if (allItems.isEmpty()) {
//                    for (Item item : ForgeRegistries.ITEMS) {
//                        NonNullList<ItemStack> subItems = NonNullList.create();
//                        item.getSubItems(CreativeTabs.SEARCH, subItems);
//                        allItems.addAll(subItems);
//                    }
//                }
                ISlot slot = getSlot(event.gui);
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

    // handle all inputs in one method
    public static boolean handleInput(@Nullable GuiContainer container) {
//        if (container != null) {// && container.isFocused()) {
//            return false;
//        }
        if (container != null && canDoShortcutAction()) {
            if (moveAll.isFirstPress() && ShortcutHandler.moveAllItems(container, false)) {
                shortcutAction();
                return true;
            }
            if (moveAllSame.isFirstPress() && ShortcutHandler.moveAllItems(container, true)) {
                shortcutAction();
                return true;
            }
            if (moveSingle.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, false)) {
                shortcutAction();
                return true;
            }
            if (moveSingleEmpty.isFirstPressOrHeldLong(15) && ShortcutHandler.moveSingleItem(container, true)) {
                shortcutAction();
                return true;
            }
            if (throwAll.isFirstPress() && ShortcutHandler.dropItems(container, false)) {
                shortcutAction();
                return true;
            }
            if (throwAllSame.isFirstPress() && ShortcutHandler.dropItems(container, true)) {
                shortcutAction();
                return true;
            }
        }
        boolean c = configGuiKey.isPressed(), s = sortKey.isPressed();
        if (c) {
            long t = Minecraft.getSystemTime();
            if (t - timeConfigGui > 500) {
                if (!ConfigGui.closeCurrent()) {
                    BogoSortAPI.INSTANCE.openConfigGui(Minecraft.getMinecraft().currentScreen);
                }
                timeConfigGui = t;
                return true;
            }
        }
        if (container != null && s) {
            long t = Minecraft.getSystemTime();
            if (t - timeSort > 500) {
                ISlot slot = getSlot(container);
                if (!canSort(slot) || !sort(container, slot)) {
                    return false;
                }
                timeSort = t;
                return true;
            }
        }
        return false;
    }

    private static boolean canSort(@Nullable ISlot slot) {
        return !Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode ||
//                sortKey.getKeyModifier().isActive(null) != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyModifier().isActive(null) ||
                sortKey.getKeyCode() != Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getKeyCode() ||
                (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() == null && (slot == null || slot.bogo$getStack() == null));
    }

    private static boolean isButtonPressed(int button) {
        return Mouse.getEventButtonState() && Mouse.getEventButton() == button;
    }

    private static boolean isKeyDown(KeyBinding key) {
//        if (!key.getKeyModifier().isActive(null)) return false;
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
    public static ISlot getSlot(GuiScreen guiScreen) {
        if (guiScreen instanceof GuiContainer) {
            return (ISlot) ((GuiContainer) guiScreen).theSlot;
        }
        return null;
    }

    public static boolean sort(GuiScreen guiScreen, @Nullable ISlot slot) {
        if (guiScreen instanceof GuiContainer) {
            Container container = ((GuiContainer) guiScreen).inventorySlots;
            GuiSortingContext sortingContext = GuiSortingContext.getOrCreate(container);
            if (sortingContext.isEmpty()) return false;
            SlotGroup slotGroup = null;
            if (slot == null) {
                if (sortingContext.getNonPlayerSlotGroupAmount() == 1) {
                    slotGroup = sortingContext.getNonPlayerSlotGroup();
                } else if (sortingContext.hasPlayer() && sortingContext.getNonPlayerSlotGroupAmount() == 0) {
                    slotGroup = sortingContext.getPlayerSlotGroup();
                }
                if (slotGroup == null || slotGroup.isEmpty()) return false;
                slot = slotGroup.getSlots().get(0);
            } else {
                slotGroup = sortingContext.getSlotGroup(slot.bogo$getSlotNumber());
                if (slotGroup == null || slotGroup.isEmpty()) return false;
            }

            List<SortRule<ItemStack>> sortRules = BogoSorterConfig.sortRules;
            boolean color = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("color"));
            boolean name = sortRules.contains(BogoSortAPI.INSTANCE.getItemSortRule("display_name"));
            NetworkHandler.sendToServer(new CSort(createSortData(slotGroup, color, name), BogoSorterConfig.sortRules, BogoSorterConfig.nbtSortRules, slot.bogo$getSlotNumber(), slotGroup.isPlayerInventory()));
//            SortHandler.playSortSound();
            System.out.println(container);
            System.out.println(sortingContext);
            System.out.println(slotGroup);
            System.out.println(sortRules);
            System.out.println(slot.bogo$getSlotNumber());
            System.out.println(slotGroup.isPlayerInventory());
            System.out.println(createSortData(slotGroup, color, name));

            return true;

        }
        return false;
    }

    public static Collection<ClientSortData> createSortData(SlotGroup slotGroup, boolean color, boolean name) {
        if (!color && !name) return Collections.emptyList();
        Map<ItemStack, ClientSortData> map = new Object2ObjectOpenCustomHashMap<>(BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);
        for (ISlot slot1 : slotGroup.getSlots()) {
            map.computeIfAbsent(slot1.bogo$getStack(), stack -> ClientSortData.of(stack, color, name)).getSlotNumbers().add(slot1.bogo$getSlotNumber());
            System.out.println("Item in slot: " + slot1.bogo$getStack());
            System.out.println("Item stack size: " + slot1.bogo$getStack().stackSize);
        }
        return map.values();
    }

    public static SortHandler createSortHandler(GuiScreen guiScreen, @Nullable ISlot slot) {
        if (slot != null && guiScreen instanceof GuiContainer) {

            Container container = ((GuiContainer) guiScreen).inventorySlots;
            boolean player = BogoSortAPI.isPlayerSlot(slot);

            if (!player && !isSortableContainer(guiScreen)) return null;

            return new SortHandler(Minecraft.getMinecraft().thePlayer, container, Int2ObjectMaps.emptyMap());
        }
        return null;
    }
}
