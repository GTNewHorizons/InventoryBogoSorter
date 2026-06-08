package com.cleanroommc.bogosorter.compat;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.bogosorter.common.network.ae2.IntegrationDiagnostics;

import appeng.api.networking.IGrid;
import appeng.api.networking.storage.IStorageGrid;
import cpw.mods.fml.common.Loader;

public final class ThaumicEnergisticsHelper {

    private static final String MOD_ID = "thaumicenergistics";
    private static final String ESSENTIA_STACK_TYPE_CLASS = "thaumicenergistics.common.storage.AEEssentiaStackType";
    private static final String ESSENTIA_STACK_TYPE_FIELD = "ESSENTIA_STACK_TYPE";
    private static final String THAUMIC_ENERGISTICS_MAIN_CLASS = "thaumicenergistics.common.ThaumicEnergistics";
    private static final String ASPECT_CLASS = "thaumcraft.api.aspects.Aspect";
    private static final AspectExtractor NO_EXTRACTOR = stack -> null;
    private static final Map<Class<?>, AspectExtractor> EXTRACTORS = new WeakHashMap<>();
    private static final KnownAspectAdapter[] KNOWN_ASPECT_ADAPTERS = new KnownAspectAdapter[] {
        new KnownAspectAdapter("thaumicenergistics.common.items.ItemCraftingAspect", "getAspect", false),
        new KnownAspectAdapter("com.gtnewhorizons.aspectrecipeindex.common.items.ItemAspect", "getAspect", false),
        new KnownAspectAdapter("com.djgiannuzz.thaumcraftneiplugin.items.ItemAspect", "getAspects", true) };

    private static Method aspectLookupMethod;
    private static boolean aspectLookupResolved;
    private static MonitorAdapter monitorAdapter;
    private static boolean monitorAdapterResolved;

    private ThaumicEnergisticsHelper() {}

    public static boolean isLoaded() {
        return Loader.isModLoaded(MOD_ID);
    }

    public static String getAspectTag(ItemStack stack) {
        if (!isLoaded() || stack == null || stack.getItem() == null) {
            return null;
        }

        String nbtAspect = validatedAspectTag(stack.getTagCompound());
        if (nbtAspect != null) {
            return nbtAspect;
        }

        AspectExtractor extractor;
        synchronized (EXTRACTORS) {
            extractor = EXTRACTORS.get(
                stack.getItem()
                    .getClass());
            if (extractor == null) {
                extractor = findExtractor(stack);
                EXTRACTORS.put(
                    stack.getItem()
                        .getClass(),
                    extractor);
            }
        }

        try {
            return extractor.extract(stack);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce(
                "thaumic-aspect-item-" + stack.getItem()
                    .getClass()
                    .getName(),
                e);
            return null;
        }
    }

    public static AmountResult getEssentiaAmount(IGrid grid, String aspectTag) {
        if (!isLoaded() || grid == null || !isValidAspectTag(aspectTag)) {
            return AmountResult.unsupported();
        }

        MonitorAdapter adapter = getMonitorAdapter();
        if (adapter == null) {
            return AmountResult.unsupported();
        }
        try {
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            if (storageGrid == null) {
                return AmountResult.error();
            }
            return AmountResult.success(adapter.getAmount(storageGrid, aspectTag));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("thaumic-essentia-monitor-runtime", e);
            return AmountResult.error();
        }
    }

    private static AspectExtractor findExtractor(ItemStack stack) {
        for (KnownAspectAdapter adapter : KNOWN_ASPECT_ADAPTERS) {
            if (adapter.supports(stack)) {
                return adapter;
            }
        }
        return NO_EXTRACTOR;
    }

    private static String validatedAspectTag(NBTTagCompound tag) {
        if (tag == null) return null;
        String aspectTag = null;
        if (tag.hasKey("Aspect")) {
            aspectTag = tag.getString("Aspect");
        } else if (tag.hasKey("AspectTag")) {
            aspectTag = tag.getString("AspectTag");
        }
        return isValidAspectTag(aspectTag) ? aspectTag : null;
    }

    private static boolean isValidAspectTag(String aspectTag) {
        if (aspectTag == null || aspectTag.isEmpty() || aspectTag.length() > 64) {
            return false;
        }
        try {
            Method lookupMethod = getAspectLookupMethod();
            return lookupMethod != null && lookupMethod.invoke(null, aspectTag) != null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("thaumcraft-aspect-validation", e);
            return false;
        }
    }

    private static Method getAspectLookupMethod() {
        if (aspectLookupResolved) {
            return aspectLookupMethod;
        }
        aspectLookupResolved = true;
        try {
            Class<?> aspectClass = Class.forName(ASPECT_CLASS);
            aspectLookupMethod = aspectClass.getMethod("getAspect", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("thaumcraft-aspect-api", e);
        }
        return aspectLookupMethod;
    }

    private static String tagOfAspect(Object aspect) throws ReflectiveOperationException {
        if (aspect == null) return null;
        Method getTag = aspect.getClass()
            .getMethod("getTag");
        Object tag = getTag.invoke(aspect);
        return tag instanceof String && isValidAspectTag((String) tag) ? (String) tag : null;
    }

    private static String singleAspectTag(Object aspectList) throws ReflectiveOperationException {
        if (aspectList == null) return null;
        Method getAspects = findMethod(aspectList.getClass(), "getAspects");
        Object aspects = getAspects.invoke(aspectList);
        if (!(aspects instanceof Object[]) || ((Object[]) aspects).length != 1) {
            return null;
        }
        return tagOfAspect(((Object[]) aspects)[0]);
    }

    private static MonitorAdapter getMonitorAdapter() {
        if (monitorAdapterResolved) {
            return monitorAdapter;
        }
        monitorAdapterResolved = true;
        try {
            Object stackType = findEssentiaStackType();
            if (stackType == null) {
                return null;
            }
            monitorAdapter = MonitorAdapter.create(stackType);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            IntegrationDiagnostics.logCapabilityFailureOnce("thaumic-essentia-monitor-adapter", e);
        }
        return monitorAdapter;
    }

    private static Object findEssentiaStackType() throws ReflectiveOperationException {
        Object direct = getEssentiaStackTypeField();
        if (isEssentiaStackType(direct)) {
            return direct;
        }
        for (String className : new String[] { ESSENTIA_STACK_TYPE_CLASS, THAUMIC_ENERGISTICS_MAIN_CLASS }) {
            try {
                Class<?> type = Class.forName(className);
                for (Field field : type.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) continue;
                    field.setAccessible(true);
                    Object found = findEssentiaStackType(field.get(null));
                    if (found != null) return found;
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    private static Object findEssentiaStackType(Object candidate) {
        if (candidate == null) return null;
        if (isEssentiaStackType(candidate)) return candidate;
        if (candidate instanceof Map<?, ?>map) {
            for (Object value : map.values()) {
                Object found = findEssentiaStackType(value);
                if (found != null) return found;
            }
        } else if (candidate instanceof Iterable<?>iterable) {
            for (Object value : iterable) {
                Object found = findEssentiaStackType(value);
                if (found != null) return found;
            }
        } else if (candidate.getClass()
            .isArray()) {
                for (int index = 0; index < Array.getLength(candidate); index++) {
                    Object found = findEssentiaStackType(Array.get(candidate, index));
                    if (found != null) return found;
                }
            }
        return null;
    }

    private static boolean isEssentiaStackType(Object candidate) {
        if (candidate == null) return false;
        try {
            Method getId = findMethod(candidate.getClass(), "getId");
            Object id = getId.invoke(candidate);
            return "essentia".equals(id);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static Object getEssentiaStackTypeField() {
        try {
            Class<?> type = Class.forName(ESSENTIA_STACK_TYPE_CLASS);
            Field field = type.getDeclaredField(ESSENTIA_STACK_TYPE_FIELD);
            field.setAccessible(true);
            return field.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | LinkageError ignored) {
            return null;
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes)
        throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        assert type != null;
        Method method = type.getMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private interface AspectExtractor {

        String extract(ItemStack stack) throws ReflectiveOperationException;
    }

    private static final class KnownAspectAdapter implements AspectExtractor {

        private final String className;
        private final String methodName;
        private final boolean returnsAspectList;
        private Class<?> itemClass;
        private Method method;
        private boolean resolved;

        private KnownAspectAdapter(String className, String methodName, boolean returnsAspectList) {
            this.className = className;
            this.methodName = methodName;
            this.returnsAspectList = returnsAspectList;
        }

        private boolean supports(ItemStack stack) {
            resolve();
            return this.itemClass != null && this.itemClass.isInstance(stack.getItem());
        }

        @Override
        public String extract(ItemStack stack) throws ReflectiveOperationException {
            resolve();
            if (this.method == null) return null;
            Object result = this.method.invoke(null, stack);
            return this.returnsAspectList ? singleAspectTag(result) : tagOfAspect(result);
        }

        private void resolve() {
            if (this.resolved) return;
            this.resolved = true;
            try {
                this.itemClass = Class.forName(this.className);
                this.method = this.itemClass.getMethod(this.methodName, ItemStack.class);
            } catch (ClassNotFoundException ignored) {
                this.itemClass = null;
                this.method = null;
            } catch (NoSuchMethodException | LinkageError e) {
                this.itemClass = null;
                this.method = null;
                IntegrationDiagnostics.logCapabilityFailureOnce(this.className + '#' + this.methodName, e);
            }
        }
    }

    private static final class MonitorAdapter {

        private final Object stackType;
        private final Method loadStackFromNbt;
        private final Map<Class<?>, Method> monitorMethods = new WeakHashMap<>();

        private MonitorAdapter(Object stackType, Method loadStackFromNbt) {
            this.stackType = stackType;
            this.loadStackFromNbt = loadStackFromNbt;
        }

        private static MonitorAdapter create(Object stackType) throws ReflectiveOperationException {
            Method loadStack = findMethod(stackType.getClass(), "loadStackFromNBT", NBTTagCompound.class);
            return new MonitorAdapter(stackType, loadStack);
        }

        private long getAmount(IStorageGrid storageGrid, String aspectTag) throws ReflectiveOperationException {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("AspectTag", aspectTag);
            tag.setLong("Cnt", 1L);
            Object requested = this.loadStackFromNbt.invoke(this.stackType, tag);
            if (requested == null) return 0L;

            Method getMeMonitor = this.monitorMethods.get(storageGrid.getClass());
            if (getMeMonitor == null) {
                getMeMonitor = findCompatibleMethod(storageGrid.getClass(), "getMEMonitor", this.stackType.getClass());
                this.monitorMethods.put(storageGrid.getClass(), getMeMonitor);
            }
            Object monitor = getMeMonitor.invoke(storageGrid, this.stackType);
            if (monitor == null) return 0L;
            Object storageList = findMethod(monitor.getClass(), "getStorageList").invoke(monitor);
            if (storageList == null) return 0L;

            Method findPrecise = findCompatibleMethod(storageList.getClass(), "findPrecise", requested.getClass());
            Object found = findPrecise.invoke(storageList, requested);
            if (found == null) return 0L;
            Object amount = findMethod(found.getClass(), "getStackSize").invoke(found);
            return amount instanceof Number ? ((Number) amount).longValue() : 0L;
        }

        private static Method findCompatibleMethod(Class<?> type, String name, Class<?> argumentType)
            throws NoSuchMethodException {
            for (Method method : type.getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (name.equals(method.getName()) && parameters.length == 1
                    && parameters[0].isAssignableFrom(argumentType)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            throw new NoSuchMethodException(name);
        }
    }

    public static final class AmountResult {

        private static final int SUCCESS = 0;
        private static final int UNSUPPORTED = 1;
        private static final int ERROR = 2;

        private final int state;
        private final long amount;

        private AmountResult(int state, long amount) {
            this.state = state;
            this.amount = amount;
        }

        public static AmountResult success(long amount) {
            return new AmountResult(SUCCESS, amount);
        }

        public static AmountResult unsupported() {
            return new AmountResult(UNSUPPORTED, 0L);
        }

        public static AmountResult error() {
            return new AmountResult(ERROR, 0L);
        }

        public boolean isSuccess() {
            return this.state == SUCCESS;
        }

        public boolean isUnsupported() {
            return this.state == UNSUPPORTED;
        }

        public long getAmount() {
            return this.amount;
        }
    }
}
