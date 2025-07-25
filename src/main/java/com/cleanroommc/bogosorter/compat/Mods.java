package com.cleanroommc.bogosorter.compat;

import java.util.function.Supplier;

import cpw.mods.fml.common.Loader;

public enum Mods {

    AdventureBackpack2("adventurebackpack"),
    Ae2("appliedenergistics2"),
    AvaritiaAddons("avaritiaddons"),
    Backhand("backhand"),
    Backpack("Backpack"),
    BetterStorage("betterstorage"),
    Bibliocraft("BiblioCraft"),
    Botania("Botania"),
    CodeChickenCore("CodeChickenCore"),
    CookingForBlockheads("cookingforblockheads"),
    DraconicEvolution("DraconicEvolution"),
    EnderStorage("EnderStorage"),
    Energycontrol("energycontrol"),
    Etfuturum("etfuturum"),
    ExtraUtilities("ExtraUtilities"),
    Forestry("Forestry"),
    GT5u(() -> Loader.isModLoaded("gregtech") && !Loader.isModLoaded("gregapi")),
    GT6(() -> Loader.isModLoaded("gregtech") && Loader.isModLoaded("gregapi")),
    GalacticraftCore("galacticraftcore"),
    HBM("hbm"),
    IC2(() -> Loader.isModLoaded("IC2") && !Loader.instance()
        .getIndexedModList()
        .get("IC2")
        .getName()
        .endsWith("Classic")),
    IC2Classic(() -> Loader.isModLoaded("IC2") && Loader.instance()
        .getIndexedModList()
        .get("IC2")
        .getName()
        .endsWith("Classic")),
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

    public final String modid;
    private final Supplier<Boolean> supplier;
    private Boolean loaded;

    Mods(String modid) {
        this.modid = modid;
        this.supplier = null;
    }

    Mods(Supplier<Boolean> supplier) {
        this.supplier = supplier;
        this.modid = null;
    }

    public boolean isLoaded() {
        if (loaded == null) {
            if (supplier != null) {
                loaded = supplier.get();
            } else if (modid != null) {
                loaded = Loader.isModLoaded(modid);
            } else loaded = false;
        }
        return loaded;
    }
}
