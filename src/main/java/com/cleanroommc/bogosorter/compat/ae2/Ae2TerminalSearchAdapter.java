package com.cleanroommc.bogosorter.compat.ae2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.config.ae2.TooltipFeatureConfig;
import com.cleanroommc.bogosorter.compat.Mods;

import codechicken.nei.PositionedStack;
import codechicken.nei.Widget;
import codechicken.nei.WidgetContainer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.NEIRecipeWidget;
import cpw.mods.fml.common.Loader;

public final class Ae2TerminalSearchAdapter {

    private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];
    private static final Class<?>[] STRING_PARAMETER = new Class<?>[] { String.class };
    private static final String AE2_NEI_MODULES_CLASS = "appeng.integration.modules.NEI";
    private static final String NEI_CONTAINER_MANAGER_CLASS = "codechicken.nei.guihook.GuiContainerManager";
    private static final String NEI_SEARCH_FIELD_CLASS = "codechicken.nei.SearchField";
    private static final Set<String> LOGGED_FAILURES = new HashSet<>();

    @Nullable
    private static GuiContainer pendingSearchGui;
    @Nullable
    private static String pendingSearchText;

    private Ae2TerminalSearchAdapter() {}

    public static boolean handleSearchKey(GuiContainer gui) {
        if (!TooltipFeatureConfig.isTerminalHoverSearchEnabled() || !Mods.Ae2.isLoaded()) {
            return false;
        }
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != BSKeybinds.ae2TerminalSearchKey.getKeyCode()) {
            return false;
        }

        GuiContainer ae2Gui = Ae2TerminalGuiDetector.resolveSearchTarget(gui);
        if (ae2Gui == null) {
            return false;
        }

        try {
            ItemStack hoveredStack = getHoveredStack(gui);
            if (hoveredStack == null) {
                return false;
            }

            String displayName = hoveredStack.getDisplayName();
            if (displayName == null || displayName.trim()
                .isEmpty()) {
                return false;
            }

            String searchText = getEscapedNeiSearchText(displayName);

            Object searchField = getFieldValue(ae2Gui, "searchField");
            if (searchField == null || isAe2SearchActive(searchField)) {
                return false;
            }

            if (gui != ae2Gui) {
                queuePendingSearch(ae2Gui, searchText);
                Minecraft.getMinecraft()
                    .displayGuiScreen(ae2Gui);
                return true;
            }

            applySearch(ae2Gui, searchText);
            return true;
        } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            logFailureOnce("terminal-search", e);
            return false;
        }
    }

    /**
     * Applies a queued terminal search after the target GUI has opened (next client tick).
     */
    public static void applyPendingSearch() {
        if (pendingSearchGui == null || pendingSearchText == null) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != pendingSearchGui) {
            return;
        }

        try {
            if (!Ae2TerminalGuiDetector.isSearchableTerminal(pendingSearchGui)) {
                clearPendingSearch();
                return;
            }
            applySearch(pendingSearchGui, pendingSearchText);
        } catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
            logFailureOnce("terminal-search-pending", e);
        } finally {
            clearPendingSearch();
        }
    }

    public static void clearPendingSearch() {
        pendingSearchGui = null;
        pendingSearchText = null;
    }

    private static void queuePendingSearch(GuiContainer gui, String searchText) {
        pendingSearchGui = gui;
        pendingSearchText = searchText;
    }

    private static void applySearch(GuiContainer ae2Gui, String searchText) throws ReflectiveOperationException {
        Object searchField = getFieldValue(ae2Gui, "searchField");
        Object repo = getFieldValue(ae2Gui, "repo");
        if (searchField == null || repo == null) {
            return;
        }

        invokeMethod(repo, "setSearchString", STRING_PARAMETER, searchText);
        invokeMethod(repo, "updateView");
        invokeMethod(searchField, "setText", STRING_PARAMETER, searchText);
        invokeMethod(searchField, "setCursorPositionEnd");
    }

    private static boolean isAe2SearchActive(Object searchField) {
        try {
            if (Boolean.TRUE.equals(invokeMethod(searchField, "isFocused"))) {
                return true;
            }
            Class<?> nei = Class.forName(AE2_NEI_MODULES_CLASS);
            Object ae2NeiSearchField = getStaticFieldValue(nei);
            return ae2NeiSearchField != null
                && Boolean.TRUE.equals(invokeMethod(ae2NeiSearchField, "existsSearchField"))
                && Boolean.TRUE.equals(invokeMethod(ae2NeiSearchField, "focused"));
        } catch (ReflectiveOperationException | LinkageError e) {
            logFailureOnce("ae2-nei-search-focus", e);
            return false;
        }
    }

    @Nullable
    private static ItemStack getHoveredStack(GuiContainer gui) {
        if (Loader.isModLoaded("NotEnoughItems")) {
            ItemStack recipeStack = getHoveredRecipeStack(gui);
            if (recipeStack != null) {
                return recipeStack;
            }

            try {
                Class<?> manager = Class.forName(NEI_CONTAINER_MANAGER_CLASS);
                Method method = manager.getMethod("getStackMouseOver", GuiContainer.class);
                Object result = method.invoke(null, gui);
                if (result instanceof ItemStack) {
                    return (ItemStack) result;
                }
            } catch (ReflectiveOperationException | LinkageError e) {
                logFailureOnce("nei-hovered-stack", e);
            }
        }
        return gui.theSlot == null ? null : gui.theSlot.getStack();
    }

    @Nullable
    private static ItemStack getHoveredRecipeStack(GuiContainer gui) {
        if (!(gui instanceof GuiRecipe)) {
            return null;
        }

        try {
            Object container = getFieldValue(gui, "container");
            if (!(container instanceof WidgetContainer)) {
                return null;
            }

            Minecraft mc = Minecraft.getMinecraft();
            int mouseX = mc.currentScreen == null ? 0 : getMouseX(mc.currentScreen.width);
            int mouseY = mc.currentScreen == null ? 0 : getMouseY(mc.currentScreen.height);
            Widget widget = ((WidgetContainer) container).getWidgetUnderMouse(mouseX, mouseY);
            if (!(widget instanceof NEIRecipeWidget)) {
                return null;
            }

            PositionedStack hovered = ((NEIRecipeWidget) widget).getPositionedStackMouseOver(mouseX, mouseY);
            return hovered == null ? null : hovered.item;
        } catch (ReflectiveOperationException | LinkageError e) {
            logFailureOnce("nei-recipe-hovered-stack", e);
            return null;
        }
    }

    private static int getMouseX(int screenWidth) {
        return Mouse.getX() * screenWidth / Minecraft.getMinecraft().displayWidth;
    }

    private static int getMouseY(int screenHeight) {
        return screenHeight - Mouse.getY() * screenHeight / Minecraft.getMinecraft().displayHeight - 1;
    }

    private static String getEscapedNeiSearchText(String text) {
        try {
            Class<?> searchField = Class.forName(NEI_SEARCH_FIELD_CLASS);
            Method method = searchField.getMethod("getEscapedSearchText", String.class);
            Object result = method.invoke(null, text);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (ReflectiveOperationException | LinkageError e) {
            logFailureOnce("nei-search-escape", e);
        }
        return text;
    }

    @Nullable
    private static Object getFieldValue(Object instance, String fieldName) throws ReflectiveOperationException {
        Class<?> current = instance.getClass();
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object getStaticFieldValue(Class<?> type) throws ReflectiveOperationException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField("searchField");
                field.setAccessible(true);
                return field.get(null);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName) throws ReflectiveOperationException {
        return invokeMethod(instance, methodName, NO_PARAMETERS);
    }

    @Nullable
    private static Object invokeMethod(Object instance, String methodName, Class<?>[] paramTypes, Object... args)
        throws ReflectiveOperationException {
        Method method = findMethod(instance.getClass(), methodName, paramTypes);
        if (method == null) {
            throw new NoSuchMethodException(methodName);
        }
        return method.invoke(instance, args);
    }

    @Nullable
    private static Method findMethod(Class<?> type, String methodName, Class<?>[] paramTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        try {
            assert type != null;
            Method method = type.getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static void logFailureOnce(String capability, Throwable throwable) {
        if (TooltipFeatureConfig.isDebugLoggingEnabled() && LOGGED_FAILURES.add(capability)) {
            BogoSorter.LOGGER.warn("AE2 terminal hover search capability {} failed", capability, throwable);
        }
    }
}
