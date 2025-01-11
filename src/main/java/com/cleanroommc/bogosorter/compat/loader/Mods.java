package com.cleanroommc.bogosorter.compat.loader;

import java.util.function.Supplier;

import cpw.mods.fml.common.Loader;

public enum Mods {

    EnderStorage("EnderStorage"),
    DraconicEvolution("DraconicEvolution"),
    AvaritiaAddons("avaritiaddons"),
    CookingForBlockheads("cookingforblockheads"),
    Ae2("appliedenergistics2"),
    Forestry("Forestry"),
    GT5u(() -> Loader.isModLoaded("gregtech") && !Loader.isModLoaded("gregapi")),
    GT6(() -> Loader.isModLoaded("gregtech") && Loader.isModLoaded("gregapi")),
    Backpack("Backpack"),
    GalacticraftCore("galacticraftcore"),
    AdventureBackpack2("adventurebackpack"),
    ProjectE("ProjectE"),
    Tconstruct("TConstruct"),
    ServerUtilities("serverutilities");
    Nutrition("nutrition"),
    Bibliocraft("BiblioCraft"),
    Mekanism("Mekanism"),
    ProjectRed("ProjRed|Expansion"),
    ImmersiveEngineering("ImmersiveEngineering"),
    Thebetweenlands("thebetweenlands"),
    Terrafirmacraft("terrafirmacraft"),
    Energycontrol("energycontrol"),
    Etfuturum("etfuturum"),
    IronChest("IronChest"),
    IC2("IC2");
   


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
