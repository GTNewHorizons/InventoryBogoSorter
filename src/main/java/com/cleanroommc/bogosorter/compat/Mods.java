package com.cleanroommc.bogosorter.compat;

import java.util.function.Predicate;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;
import cpw.mods.fml.common.versioning.VersionRange;

public enum Mods {

    // spotless:off
    AdventureBackpack2("adventurebackpack"),
    Ae2("appliedenergistics2"),
    AvaritiaAddons("avaritiaddons"),
    Backhand("backhand"),
    Backpack("Backpack"),
    BetterStorage("betterstorage", versionMatches("(,0.14)")),
    BetterStorageFixed("betterstorage", versionMatches("[0.14,)")),
    Bibliocraft("BiblioCraft"),
    Botania("Botania"),
    Buildcraft("BuildCraft|Core"),
    CodeChickenCore("CodeChickenCore"),
    CookingForBlockheads("cookingforblockheads"),
    Chisel("chisel"),
    DraconicEvolution("DraconicEvolution"),
    EnderStorage("EnderStorage"),
    Energycontrol("energycontrol"),
    Etfuturum("etfuturum"),
    ExtraUtilities("ExtraUtilities"),
    Forestry("Forestry"),
    GT5u("gregtech", mod -> !Loader.isModLoaded("gregapi")),
    GT6("gregtech", mod -> Loader.isModLoaded("gregapi")),
    GalacticraftCore("galacticraftcore"),
    HBM("hbm"),
    IC2("IC2", mod -> !mod.getName().endsWith("Classic")),
    IC2Classic("IC2", mod -> mod.getName().endsWith("Classic")),
    ImmersiveEngineering("ImmersiveEngineering"),
    IronChest("IronChest"),
    Mekanism("Mekanism"),
    Nutrition("nutrition"),
    ProjectE("ProjectE"),
    ProjectRed("ProjRed|Expansion"),
    ServerUtilities("serverutilities"),
    StorageDrawers("StorageDrawers"),
    Tconstruct("TConstruct"),
    Terrafirmacraft("terrafirmacraft"),
    Thebetweenlands("thebetweenlands"),
    ActuallyAdditions("ActuallyAdditions"),
    Controlling("controlling"),

    ;
    //spotless:on

    private final String modid;
    private final Predicate<ModContainer> modPredicate;
    private Boolean loaded;

    Mods(String modid) {
        this(modid, mod -> true); // Default: any version is OK
    }

    Mods(String modid, Predicate<ModContainer> predicate) {
        this.modid = modid;
        this.modPredicate = predicate;
    }

    public boolean isLoaded() {
        if (loaded != null) return loaded;
        ModContainer mod = Loader.instance()
            .getIndexedModList()
            .get(modid);
        if (mod == null) return loaded = false;
        return loaded = Loader.isModLoaded(modid) && modPredicate.test(mod);
    }

    /**
     * Creates a predicate that checks if a mod's version matches a given version range string.
     *
     * @param range A standard Maven version range string. Examples:
     *              <ul>
     *              <li>{@code "[2.2,)"} - Version 2.2 or higher</li>
     *              <li>{@code "(,2.1]"} - Version 2.1 or lower</li>
     *              <li>{@code "[2.0,3.0]"} - Version 2.0 up to and including 3.0</li>
     *              <li>{@code "[2.0,3.0)"} - Version 2.0 up to, but excluding, 3.0</li>
     *              <li>{@code "(,3.0)"} - Any version up to, but excluding, 3.0</li>
     *              <li>{@code "(2.0,3.0)"} - Any version between 2.0 and 3.0, exclusive</li>
     *              </ul>
     * @return A predicate for use in the enum constructor.
     */
    public static Predicate<ModContainer> versionMatches(String range) {
        VersionRange versionRange = VersionParser.parseRange(range);
        return mod -> {
            ArtifactVersion modVersion = mod.getProcessedVersion();
            return versionRange.containsVersion(modVersion);
        };
    }
}
