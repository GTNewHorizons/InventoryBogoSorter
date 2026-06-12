package com.cleanroommc.bogosorter.mixinplugin;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    Vanilla(new MixinBuilder()
        .setPhase(Phase.EARLY)
        .addCommonMixins(
            "minecraft.ContainerHorseInventoryMixin",
            "minecraft.MixinItemStack",
            "minecraft.SlotMixin",
            "minecraft.SlotAccessor")
        .addClientMixins(
            "minecraft.CreativeSlotMixin",
            "minecraft.MixinGuiKeyBindingList",
            "minecraft.GuiKeyBindingListKeyEntryAccessor",
            "minecraft.GuiScreenAccessor",
            "minecraft.GuiContainerAccessor",
            "minecraft.GuiEditSignMixin",
            "minecraft.MinecraftMixin")),
    IronChest(new MixinBuilder()
        .addRequiredMod(TargetedMod.IRONCHEST)
        .setPhase(Phase.LATE)
        .addCommonMixins("ironchests.MixinIronChestContainer")),
    EnderIo(new MixinBuilder()
        .addRequiredMod(TargetedMod.ENDERIO)
        .setPhase(Phase.LATE)
        .addCommonMixins(
            "enderio.MixinVacuumChest",
            "enderio.MixinBuffer")),
    GalacticraftCore(new MixinBuilder()
        .addRequiredMod(TargetedMod.GALACTICRAFTCORE)
        .setPhase(Phase.LATE)
        .addCommonMixins(
            "galacticraft.core.MixinContainerRocketInventory",
            "galacticraft.planets.MixinContainerSlimeling")),
    ThermalExpansion(new MixinBuilder()
        .addRequiredMod(TargetedMod.THERMALEXPANSION)
        .setPhase(Phase.LATE)
        .addCommonMixins(
            "thermal.MixinContainerSatchel",
            "thermal.MixinContainerStrongbox")),
    CompactStorage(new MixinBuilder()
        .addRequiredMod(TargetedMod.COMPACTSTORAGE)
        .setPhase(Phase.LATE)
        .addCommonMixins(
            "compactstorage.MixinContainerChest",
            "compactstorage.MixinContainerChestBuilder")),
    EtFuturum(new MixinBuilder()
        .addRequiredMod(TargetedMod.ETFUTURUM)
        .setPhase(Phase.LATE)
        .addCommonMixins("etfuturum.MixinContainerChestGeneric")),
    Controlling(new MixinBuilder()
        .addRequiredMod(TargetedMod.CONTROLLING)
        .setPhase(Phase.LATE)
        .addClientMixins(
            "controlling.MixinGuiNewKeyBindingList",
            "controlling.GuiNewKeyBindingListKeyEntryAccessor")
    );
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
