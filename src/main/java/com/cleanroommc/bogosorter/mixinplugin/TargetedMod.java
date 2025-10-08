package com.cleanroommc.bogosorter.mixinplugin;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    AE2("appeng.transformer.AppEngCore", "appliedenergistics2"),
    CODECHICKENCORE("codechicken.core.launch.CodeChickenCorePlugin", "CodeChickenCore"),
    COMPACTSTORAGE(null, "compactstorage"),
    ENDERIO(null, "EnderIO"),
    ETFUTURUM(null, "etfuturum"),
    FORESTRY(null, "Forestry"),
    GALACTICRAFTCORE("micdoodle8.mods.galacticraft.core.asm.GCLoadingPlugin", "GalacticraftCore"),
    IRONCHEST(null, "IronChest"),
    NEI("codechicken.nei.asm.NEICorePlugin", "NotEnoughItems"),
    THERMALEXPANSION(null, "ThermalExpansion"),
    CONTROLLING(null, "controlling");

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
