package com.cleanroommc.bogosorter.core;


import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.mixinplugin.Mixins;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.Name("BogoSorter-Core")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class BogoSorterCore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public static final Logger LOGGER = LogManager.getLogger(BogoSorter.ID);

    private static Boolean isDevEnv;

    public static boolean isDevEnv() {
        return isDevEnv;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.bogosorter.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Mixins.getEarlyMixins(loadedCoreMods);
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
    public void injectData(Map<String, Object> data) {
        isDevEnv = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
