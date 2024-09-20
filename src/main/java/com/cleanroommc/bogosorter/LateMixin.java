//package com.cleanroommc.bogosorter;
//
//import com.google.common.collect.ImmutableList;
//
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class LateMixin implements ILateMixinLoader {
//
//    public static final List<String> modMixins = ImmutableList.of("ironchest", "thermalexpansion", "enderio",
//            "gregtechceu", "colossalchests", "galacticraftcore", "galacticraftplanets", "charset",
//            "simplybackpacks", "avaritiaddons", "improvedbackpacks", "quark", "rftools");
//
//    @Override
//    public List<String> getMixinConfigs() {
//        return modMixins.stream().map(mod -> "mixin.bogosorter." + mod + ".json").collect(Collectors.toList());
//    }
//
//    @Override
//    public boolean shouldMixinConfigQueue(String mixinConfig) {
//        String[] parts = mixinConfig.split("\\.");
//        return parts.length != 4 || shouldEnableModMixin(parts[2]);
//    }
//
//    @SuppressWarnings("all")
//    public boolean shouldEnableModMixin(String mod) {
//        if ("gregtechceu".equals(mod)) {
//            return Loader.isModLoaded("gregtech") && GregTechVersion.MAJOR >= 2;
//        }
//        return Loader.isModLoaded(mod);
//    }
//}
