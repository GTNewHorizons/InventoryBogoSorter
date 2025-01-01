package com.cleanroommc.bogosorter.compat.loader;

import cpw.mods.fml.common.Loader;

public enum Mods {

    EnderStorage("EnderStorage"),
    DraconicEvolution("DraconicEvolution"),
    AvaritiaAddons("avaritiaddons"),
    CookingForBlockheads("cookingforblockheads"),
    Ae2("appliedenergistics2"),
    Forestry("Forestry"),
    GT5u("gregtech"),
    Backpack("Backpack"),
    GalacticraftCore("galacticraftcore"),
    AdventureBackpack2("adventurebackpack"),
    ProjectE("ProjectE"),
    Tconstruct("TConstruct"),;

    public final String modid;
    private Boolean loaded;

    Mods(String modid) {
        this.modid = modid;
    }

    public boolean isLoaded() {
        if (loaded == null) {
            loaded = Loader.isModLoaded(modid);
        }
        return loaded;
    }
}
