package com.cleanroommc.bogosorter.compat.loader;

import java.util.function.Supplier;

import cpw.mods.fml.common.Loader;

public enum Mods {

    AdventureBackpack2("adventurebackpack"),
    Ae2("appliedenergistics2"),
    AvaritiaAddons("avaritiaddons"),
    Backpack("Backpack"),
    Bibliocraft("BiblioCraft"),
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
    IC2("IC2"),
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
    Thebetweenlands("thebetweenlands"),;

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
