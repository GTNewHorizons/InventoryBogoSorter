package com.cleanroommc.bogosorter.compat;

import java.util.function.Predicate;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public enum Mods {

    // spotless:off
    AdventureBackpack2("adventurebackpack"),
    Ae2("appliedenergistics2"),
    AvaritiaAddons("avaritiaddons"),
    Backhand("backhand"),
    Backpack("Backpack"),
    BetterStorage("betterstorage", mod -> mod.getVersion().startsWith("0.13")),
    BetterStorageFixed("betterstorage", mod -> mod.getVersion().startsWith("0.14")),
    Bibliocraft("BiblioCraft"),
    Botania("Botania"),
    Buildcraft("BuildCraft|Core"),
    CodeChickenCore("CodeChickenCore"),
    CookingForBlockheads("cookingforblockheads"),
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
}
