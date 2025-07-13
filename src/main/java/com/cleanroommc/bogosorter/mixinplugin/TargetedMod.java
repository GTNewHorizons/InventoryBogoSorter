package com.cleanroommc.bogosorter.mixinplugin;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    AVARITIADDONS(null, "avaritiaddons"),
    IRONCHEST(null, "IronChest"),
    ENDERIO(null, "EnderIO"),
    GALACTICRAFTCORE("micdoodle8.mods.galacticraft.core.asm.GCLoadingPlugin", "GalacticraftCore"),
    THERMALEXPANSION(null, "ThermalExpansion"),
    Forestry(null, "Forestry"),
    CodeChickenCore("codechicken.core.launch.CodeChickenCorePlugin", "CodeChickenCore"),
    NEI("codechicken.nei.asm.NEICorePlugin", "NotEnoughItems"),
    AE2("appeng.transformer.AppEngCore", "appliedenergistics2"),
    CompactStorage(null, "compactstorage"),
    Etfuturum(null, "etfuturum");

    private final TargetModBuilder builder;

    TargetedMod(String coreModClass, String modId) {
        this.builder = new TargetModBuilder().setCoreModClass(coreModClass)
            .setModId(modId);
    }

    @NotNull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
