package com.cleanroommc.bogosorter.mixinplugin;

public enum TargetedMod {

    VANILLA("Minecraft", null),
    AVARITIADDONS("Avaritiaddons", null, "avaritiaddons"),
    IRONCHEST("Iron Chests", null, "IronChest"),
    ENDERIO("Ender IO", null, "EnderIO"),
    GALACTICRAFTCORE("Galacticraft Core", "micdoodle8.mods.galacticraft.core.asm.GCLoadingPlugin", "GalacticraftCore"),
    THERMALEXPANSION("Thermal Expansion", null, "ThermalExpansion"),
    Forestry("Forestry", null, "Forestry"),
    CodeChickenCore("CodeChickenCore", "codechicken.core.launch.CodeChickenCorePlugin", "CodeChickenCore"),
    NEI("NotEnoughItems", "codechicken.nei.asm.NEICorePlugin", "NotEnoughItems"),
    AE2("Applied Energistics 2", "appeng.transformer.AppEngCore", "appliedenergistics2"),;

    /** The "name" in the @Mod annotation */
    public final String modName;
    /** Class that implements the IFMLLoadingPlugin interface */
    public final String coreModClass;
    /** The "modid" in the @Mod annotation */
    public final String modId;

    TargetedMod(String modName, String coreModClass) {
        this(modName, coreModClass, null);
    }

    TargetedMod(String modName, String coreModClass, String modId) {
        this.modName = modName;
        this.coreModClass = coreModClass;
        this.modId = modId;
    }

    @Override
    public String toString() {
        return "TargetedMod{modName='" + modName + "', coreModClass='" + coreModClass + "', modId='" + modId + "'}";
    }
}
