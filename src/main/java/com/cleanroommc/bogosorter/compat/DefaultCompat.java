package com.cleanroommc.bogosorter.compat;

import static com.cleanroommc.bogosorter.compat.Mods.*;

import java.util.ArrayList;
import java.util.List;

import net.blay09.mods.cookingforblockheads.container.ContainerCounter;
import net.blay09.mods.cookingforblockheads.container.ContainerFridge;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

import com.bioxx.tfc.Containers.ContainerChestTFC;
import com.brandon3055.draconicevolution.common.container.ContainerDraconiumChest;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.mixins.early.minecraft.SlotAccessor;
import com.hbm.inventory.container.ContainerCrateDesh;
import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.inventory.container.ContainerCrateSteel;
import com.hbm.inventory.container.ContainerCrateTemplate;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.container.ContainerSafe;
import com.rwtema.extrautils.gui.ContainerHoldingBag;
import com.zuxelus.energycontrol.containers.ContainerCardHolder;

import appeng.container.implementations.ContainerSkyChest;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import codechicken.enderstorage.storage.item.ContainerEnderItemStorage;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerGiantChest;
import de.eydamos.backpack.inventory.container.Boundaries;
import de.eydamos.backpack.inventory.container.ContainerAdvanced;
import forestry.core.gui.ContainerNaturalistInventory;
import forestry.storage.gui.ContainerBackpack;
import forestry.storage.gui.ContainerNaturalistBackpack;
import gregapi.gui.ContainerCommonChest;
import gregapi.gui.ContainerCommonDefault;
import ic2.core.block.personal.ContainerPersonalChest;
import ic2.core.item.tool.ContainerToolbox;
import jds.bibliocraft.blocks.ContainerFramedChest;
import micdoodle8.mods.galacticraft.core.inventory.ContainerParaChest;
import moze_intel.projecte.gameObjs.container.AlchBagContainer;
import moze_intel.projecte.gameObjs.container.AlchChestContainer;
import moze_intel.projecte.gameObjs.container.CondenserContainer;
import moze_intel.projecte.gameObjs.container.CondenserMK2Container;
import tconstruct.armor.inventory.KnapsackContainer;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.inventory.PartCrafterChestContainer;
import tconstruct.tools.inventory.PatternChestContainer;
import team.chisel.inventory.ContainerPresent;
import thebetweenlands.inventory.container.ContainerLurkerSkinPouch;
import wanion.avaritiaddons.block.chest.compressed.ContainerCompressedChest;

public class DefaultCompat {

    public static void init(IBogoSortAPI api) {
        // vanilla
        api.addCompat(ContainerPlayer.class, (container, builder) -> {
            // player slots are automatically added
        });
        // vanilla crafting table has issues with ctrl+click, so its not here
        api.addPlayerSortButtonPosition(ContainerPlayer.class, (slotGroup, buttonPos) -> {
            if (Nutrition.isLoaded()) {
                IPosSetter.TOP_RIGHT_VERTICAL.setButtonPos(slotGroup, buttonPos);
            } else {
                IPosSetter.TOP_RIGHT_HORIZONTAL.setButtonPos(slotGroup, buttonPos);
            }
        });
        api.addGenericCompat(ContainerChest.class);

        api.addCompat(
            ContainerDispenser.class,
            (container, builder) -> {
                builder.addGenericSlotGroup()
                    .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
            });
        api.addGenericCompat(ContainerHopper.class);

        if (EnderStorage.isLoaded()) {
            api.addCompat(ContainerEnderItemStorage.class, (container, builder) -> {
                switch (container.chestInv.getSize()) {
                    case 0:
                        builder.addSlotGroup(0, 9, 3);
                        break;
                    case 1:
                        builder.addSlotGroup(0, 27, 9);
                        break;
                    case 2:
                        builder.addSlotGroup(0, 54, 9);
                        break;
                }
            });
        }

        if (Ae2.isLoaded()) {
            api.addCompat(ContainerSkyChest.class, (container, builder) -> { builder.addSlotGroup(0, 36, 9); });
        }

        if (DraconicEvolution.isLoaded()) {
            api.addCompatSimple(
                ContainerDraconiumChest.class,
                (container, builder) -> { builder.addSlotGroup(0, 234, 26); });
            api.addPlayerSortButtonPosition(ContainerDraconiumChest.class, (slotGroup, buttonPos) -> {
                SlotAccessor topRight = slotGroup.getSlots()
                    .get(slotGroup.getRowSize() - 1);
                buttonPos.setVertical();
                buttonPos.setTopLeft();
                buttonPos.setPos(topRight.bogo$getX() + 17, topRight.bogo$getY() - 1);
            });
        }

        if (Backpack.isLoaded()) {
            api.addCompat(ContainerAdvanced.class, (container, builder) -> {
                int rowSize = getRowSize(container.getBoundary(Boundaries.BACKPACK_END));
                builder.addSlotGroup(
                    container.getBoundary(Boundaries.BACKPACK),
                    container.getBoundary(Boundaries.BACKPACK_END),
                    rowSize);
            });
        }

        if (AdventureBackpack2.isLoaded()) {
            api.addCompat(
                com.darkona.adventurebackpack.inventory.ContainerBackpack.class,
                (container, builder) -> { builder.addSlotGroup(36, 84, 8); });
        }

        if (ProjectE.isLoaded()) {
            api.addCompat(
                AlchBagContainer.class,
                (container, builder) -> {
                    builder.addSlotGroup(0, 104, 13)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addCompat(
                AlchChestContainer.class,
                (container, builder) -> {
                    builder.addSlotGroup(0, 104, 13)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addCompat(CondenserContainer.class, (container, builder) -> { builder.addSlotGroup(1, 92, 13); });
            api.addCompat(
                CondenserMK2Container.class,
                (container, builder) -> {
                    builder.addSlotGroup(1, 43, 6)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addPlayerSortButtonPosition(AlchBagContainer.class, IPosSetter.TOP_RIGHT_VERTICAL);
            api.addPlayerSortButtonPosition(AlchChestContainer.class, IPosSetter.TOP_RIGHT_VERTICAL);
            api.addPlayerSortButtonPosition(CondenserContainer.class, IPosSetter.TOP_RIGHT_VERTICAL);
            api.addPlayerSortButtonPosition(CondenserMK2Container.class, IPosSetter.TOP_RIGHT_VERTICAL);
        }

        if (ImmersiveEngineering.isLoaded()) {
            api.addCompat(ContainerCrate.class, (container, builder) -> { builder.addSlotGroup(0, 27, 9); });
        }

        if (Forestry.isLoaded()) {
            api.addCompat(ContainerBackpack.class, (container, builder) -> {
                if (container.inventorySlots.size() == 51) {
                    builder.addSlotGroup(36, container.inventorySlots.size(), 5);
                } else {
                    builder.addSlotGroup(36, container.inventorySlots.size(), 9);

                }
            });
            api.addCompat(ContainerNaturalistBackpack.class, (container, builder) -> {
                List<Slot> slots = new ArrayList<>();
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        slots.add(container.getSlot(i * 5 + j + 36));
                    }
                }
                builder.addSlotGroupOf(slots, 5)
                    .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
            });
            api.addCompat(ContainerNaturalistInventory.class, (container, builder) -> {
                List<Slot> slots = new ArrayList<>();
                for (int i = 0; i < 25; i++) {
                    for (int j = 0; j < 5; j++) {
                        slots.add(container.getSlot(i * 5 + j + 36));
                    }
                }
                builder.addSlotGroupOf(slots, 5)
                    .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
            });
            api.addPlayerSortButtonPosition(ContainerNaturalistBackpack.class, IPosSetter.TOP_RIGHT_VERTICAL);
            api.addPlayerSortButtonPosition(ContainerNaturalistInventory.class, IPosSetter.TOP_RIGHT_VERTICAL);
        }

        if (IC2.isLoaded()) {
            api.addGenericCompat(ContainerPersonalChest.class);
            api.addCompat(ContainerToolbox.class, (container, builder) -> { builder.addSlotGroup(0, 9, 9); });
        }

        if (IC2Classic.isLoaded()) {
            api.addGenericCompat(ContainerPersonalChest.class);
        }

        if (Bibliocraft.isLoaded()) {
            api.addCompat(
                ContainerFramedChest.class,
                (container, builder) -> {
                    builder.addSlotGroup(
                        0,
                        container.getMainTile()
                            .getIsDouble() ? 27 * 2 : 27,
                        9);
                });
        }

        if (Energycontrol.isLoaded()) {
            api.addCompat(ContainerCardHolder.class, (container, builder) -> { builder.addSlotGroup(0, 54, 9); });
        }

        if (ProjectRed.isLoaded()) {
            api.addCompat(
                mrtjp.projectred.exploration.ContainerBackpack.class,
                (container, builder) -> { builder.addSlotGroup(0, 27, 9); });
        }

        if (Thebetweenlands.isLoaded()) {
            api.addCompat(ContainerLurkerSkinPouch.class, (container, builder) -> {
                IInventory inventory = container.inventory;
                builder.addSlotGroup(0, inventory.getSizeInventory(), 9);
            });
        }

        if (Terrafirmacraft.isLoaded()) {
            api.addCompat(
                ContainerChestTFC.class,
                (container, builder) -> {
                    builder.addSlotGroup(
                        0,
                        container.getLowerChestInventory()
                            .getSizeInventory(),
                        9);
                });
        }

        if (GalacticraftCore.isLoaded()) {
            api.addCompat(ContainerParaChest.class, (container, builder) -> {
                int slot = container.getparachestInventory()
                    .getSizeInventory() - 3;

                if (slot > 0) {
                    builder.addSlotGroup(0, slot, 9);
                }
            });
        }
        if (AvaritiaAddons.isLoaded()) {
            api.addCompatSimple(
                ContainerCompressedChest.class,
                (container, builder) -> { builder.addSlotGroup(0, 243, 27); });
            //
            // //todo compat with infinity chest
            // api.addCompatSimple(ContainerInfinityChest.class, (container, builder) -> {
            // builder.addSlotGroup(0, 9 * 27, 27);
            // });
            // api.addPlayerSortButtonPosition(ContainerInfinityChest.class, (slotGroup, buttonPos) -> {
            // SlotAccessor topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
            // buttonPos.setVertical();
            // buttonPos.setTopLeft();
            // buttonPos.setPos(topRight.bogo$getX() + 17, topRight.bogo$getY() - 1);
            // });
        }

        if (CookingForBlockheads.isLoaded()) {
            api.addGenericCompat(ContainerCounter.class);
            api.addGenericCompat(ContainerFridge.class);
        }

        if (Mekanism.isLoaded()) {
            api.addGenericCompat(mekanism.common.inventory.container.ContainerPersonalChest.class);
        }

        if (Tconstruct.isLoaded()) {
            api.addGenericCompat(PatternChestContainer.class);
            api.addGenericCompat(KnapsackContainer.class);
            api.addCompat(PartCrafterChestContainer.class, (container, builder) -> { builder.addSlotGroup(8, 38, 6); });
            api.addCompat(CraftingStationContainer.class, (container, builder) -> {
                if (container.inventorySlots.size() > 51) {
                    builder.addSlotGroup(46, container.inventorySlots.size(), 6)
                        .buttonPosSetter((slotGroup, buttonPos) -> {
                            buttonPos.setPos(0, 1000);
                            for (SlotAccessor slot : slotGroup.getSlots()) {
                                if (slot.bogo$getX() >= 0 && slot.bogo$getY() >= 0 && slot.callIsEnabled()) {
                                    buttonPos.setPos(
                                        Math.max(buttonPos.getX(), slot.bogo$getX() + 17),
                                        Math.min(buttonPos.getY(), slot.bogo$getY() - 2));
                                }
                            }
                        });
                }
            });
        }
        if (ExtraUtilities.isLoaded()) {
            api.addCompat(ContainerHoldingBag.class, (container, builder) -> { builder.addSlotGroup(0, 54, 9); });

        }
        if (HBM.isLoaded()) {
            api.addCompat(ContainerCrateDesh.class, (container, builder) -> { builder.addSlotGroup(0, 104, 13); });
            api.addCompat(ContainerCrateSteel.class, (container, builder) -> { builder.addSlotGroup(0, 54, 9); });
            api.addCompat(ContainerCrateIron.class, (container, builder) -> { builder.addSlotGroup(0, 36, 9); });
            api.addCompat(ContainerCrateTungsten.class, (container, builder) -> { builder.addSlotGroup(0, 27, 9); });
            api.addCompat(ContainerCrateTemplate.class, (container, builder) -> { builder.addSlotGroup(0, 27, 9); });
            api.addCompat(ContainerSafe.class, (container, builder) -> { builder.addSlotGroup(0, 15, 5); });

        }
        if (BetterStorage.isLoaded()) {
            api.addCompat(
                net.mcft.copy.betterstorage.container.ContainerCraftingStation.class,
                (container, builder) -> {
                    builder.addSlotGroup(18, 36, 9)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addCompat(
                net.mcft.copy.betterstorage.container.ContainerCrate.class,
                (container, builder) -> {
                    builder.addGenericSlotGroup()
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addGenericCompat(net.mcft.copy.betterstorage.container.ContainerBetterStorage.class);
        }

        if (BetterStorageFixed.isLoaded()) {
            api.addCompat(
                net.mcft.betterstorage.container.ContainerCraftingStation.class,
                (container, builder) -> {
                    builder.addSlotGroup(18, 36, 9)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addCompat(
                net.mcft.betterstorage.container.ContainerCrate.class,
                (container, builder) -> {
                    builder.addGenericSlotGroup()
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addGenericCompat(net.mcft.betterstorage.container.ContainerBetterStorage.class);
        }

        if (ActuallyAdditions.isLoaded()) {
            api.addCompat(
                ContainerGiantChest.class,
                (container, builder) -> {
                    builder.addSlotGroup(0, 117, 13)
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addPlayerSortButtonPosition(ContainerGiantChest.class, IPosSetter.TOP_RIGHT_VERTICAL);
        }

        if (GT6.isLoaded()) {
            api.addGenericCompat(ContainerCommonChest.class);
            api.addCompat(
                ContainerCommonDefault.class,
                (container, builder) -> {
                    builder.addGenericSlotGroup()
                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
                });
            api.addPlayerSortButtonPosition(ContainerCommonDefault.class, IPosSetter.TOP_RIGHT_VERTICAL);
        }

        if (Chisel.isLoaded()) {
            api.addGenericCompat(ContainerPresent.class);
        }
    }

    private static int getRowSize(int size) {
        if (size < 64) {
            return 9;
        } else {
            // Find the smallest columns value that fits within 7 rows
            for (int columns = 9; columns <= 19; columns++) {
                int rows = (size + columns - 1) / columns;
                if (rows <= 7) {
                    return columns;
                }
            }
        }
        return 1; // Fallback to 1 column if no suitable size is found
    }
}
