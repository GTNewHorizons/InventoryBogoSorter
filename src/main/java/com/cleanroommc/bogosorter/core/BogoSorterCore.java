package com.cleanroommc.bogosorter.core;

import com.cleanroommc.bogosorter.mixins.Mixins;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.Name("BogoSorter-Core")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class BogoSorterCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static Boolean isDevEnv;

    public static boolean isDevEnv() {
        return isDevEnv;
    }


    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {isDevEnv = !(boolean) data.get("runtimeDeobfuscationEnabled");}

    @Override
    public String getAccessTransformerClass() {
        return "com.cleanroommc.bogosorter.core.BogoSorterTransformer";
    }

    @Override
    public String getMixinConfig() {
        return "mixins.bogosorter.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Mixins.getEarlyMixins(loadedCoreMods);
    }
}
