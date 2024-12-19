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
    AdventureBackpack2("adventurebackpack"),;

    public final String modid;
    private boolean loaded;

    Mods(String modid) {
        this.modid = modid;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void check() {
        this.loaded = Loader.isModLoaded(modid);
    }
}
