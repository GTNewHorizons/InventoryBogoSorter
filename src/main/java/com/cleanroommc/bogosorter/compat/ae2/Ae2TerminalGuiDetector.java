package com.cleanroommc.bogosorter.compat.ae2;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.gui.inventory.GuiContainer;

import org.jetbrains.annotations.Nullable;

// class for attempting at terminal detection for ae2
public final class Ae2TerminalGuiDetector {

    private static final Class<?>[] NO_PARAMETERS = new Class<?>[0];
    private static final Class<?>[] STRING_PARAMETER = new Class<?>[] { String.class };

    // just known
    private static final String[] KNOWN_SEARCHABLE_TERMINAL_CLASSES = {
        "appeng.client.gui.implementations.GuiMEMonitorable", "appeng.client.gui.implementations.GuiWirelessTerm",
        "appeng.client.gui.implementations.GuiCraftingTerm", "appeng.client.gui.implementations.GuiPatternTerm",
        "appeng.client.gui.implementations.GuiCraftingCPU",
        // Thaumic
        "thaumicenergistics.client.gui.GuiArcaneCraftingTerminal",
        // AE2FC
        "com.glodblock.github.client.gui.base.FCBaseMEGui", };

    private static final Map<Class<?>, Boolean> SEARCH_CAPABILITY_CACHE = new ConcurrentHashMap<>();

    private Ae2TerminalGuiDetector() {}

    public static boolean isSearchableTerminal(@Nullable Object gui) {
        return asSearchableTerminal(gui) != null;
    }

    @Nullable
    public static GuiContainer resolveSearchTarget(@Nullable GuiContainer gui) {
        if (gui == null) {
            return null;
        }
        if (isSearchableTerminal(gui)) {
            return gui;
        }

        GuiContainer nested = asSearchableTerminal(readMember(gui));
        if (nested != null) {
            return nested;
        }

        return asSearchableTerminal(invokeOptionalNoArg(gui));
    }

    @Nullable
    private static GuiContainer asSearchableTerminal(@Nullable Object gui) {
        if (!(gui instanceof GuiContainer container)) {
            return null;
        }
        if (matchesKnownTerminalClass(container) || hasSearchCapability(container)) {
            return container;
        }
        return null;
    }

    private static boolean matchesKnownTerminalClass(GuiContainer gui) {
        Class<?> current = gui.getClass();
        while (current != null) {
            String name = current.getName();
            for (String known : KNOWN_SEARCHABLE_TERMINAL_CLASSES) {
                if (known.equals(name)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private static boolean hasSearchCapability(GuiContainer gui) {
        return SEARCH_CAPABILITY_CACHE.computeIfAbsent(gui.getClass(), Ae2TerminalGuiDetector::probeSearchCapability);
    }

    private static boolean probeSearchCapability(Class<?> guiClass) {
        Field searchFieldField = findFieldDecl(guiClass, "searchField");
        Field repoField = findFieldDecl(guiClass, "repo");
        if (searchFieldField == null || repoField == null) {
            return false;
        }
        Class<?> searchFieldType = searchFieldField.getType();
        Class<?> repoType = repoField.getType();
        if (findMethod(repoType, "setSearchString", STRING_PARAMETER) == null) {
            return false;
        }
        if (findMethod(repoType, "updateView", NO_PARAMETERS) == null) {
            return false;
        }
        return findMethod(searchFieldType, "setText", STRING_PARAMETER) != null;
    }

    @Nullable
    private static Field findFieldDecl(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    @Nullable
    private static Object readMember(Object instance) {
        try {
            Field field = findFieldDecl(instance.getClass(), "firstGui");
            if (field == null) {
                return null;
            }
            return field.get(instance);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    @Nullable
    private static Object invokeOptionalNoArg(Object instance) {
        try {
            Method method = findMethod(instance.getClass(), "getFirstScreen", NO_PARAMETERS);
            return method == null ? null : method.invoke(instance);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
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
}
