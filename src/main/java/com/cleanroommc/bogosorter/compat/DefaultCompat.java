package com.cleanroommc.bogosorter.compat;


import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.api.IBogoSortAPI;
import com.cleanroommc.bogosorter.api.IPosSetter;
import cpw.mods.fml.common.Loader;


import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.ContainerPlayer;

public class DefaultCompat {

    public static void init(IBogoSortAPI api) {
        // vanilla
        api.addCompat(ContainerPlayer.class, (container, builder) -> {
            // player slots are automatically added
        });
        api.addPlayerSortButtonPosition(ContainerPlayer.class, (slotGroup, buttonPos) -> {
            if (BogoSorter.isQuarkLoaded() || Loader.isModLoaded("nutrition")) {
                IPosSetter.TOP_RIGHT_VERTICAL.setButtonPos(slotGroup, buttonPos);
            } else {
                IPosSetter.TOP_RIGHT_HORIZONTAL.setButtonPos(slotGroup, buttonPos);
            }
        });
        api.addCompat(ContainerChest.class, (container, builder) -> {
            // quark adds a search bar
            builder.addGenericSlotGroup()
                .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL);
        });
        api.addCompat(ContainerDispenser.class, (container, builder) -> {
            builder.addGenericSlotGroup()
                .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
        });
        api.addCompat(ContainerHopper.class, (container, builder) -> {
            builder.addGenericSlotGroup();
        });
//        api.addCompat(ContainerShulkerBox.class, (container, builder) -> {
//            builder.addGenericSlotGroup()
//                    .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL);
//
//        });
        // for horse inventory compat see MixinContainerHorseInventory

//        if (Loader.isModLoaded("actuallyadditions")) {
//            api.addCompat(ContainerGiantChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, 117, 13);
//            });
//            // TODO slightly clashes with page button
//            api.addPlayerSortButtonPosition(ContainerGiantChest.class, IPosSetter.TOP_RIGHT_VERTICAL);
//        }
//
//        if (Loader.isModLoaded("enderstorage")) {
//            api.addCompat(ContainerEnderItemStorage.class, (container, builder) -> {
//                switch (container.chestInv.getSize()) {
//                    case 0:
//                        builder.addSlotGroup(0, 9, 3);
//                        break;
//                    case 1:
//                        builder.addSlotGroup(0, 27, 9);
//                        break;
//                    case 2:
//                        builder.addSlotGroup(0, 54, 9);
//                        break;
//                }
//            });
//        }
//
//        if (Loader.isModLoaded("appliedenergistics2")) {
//            api.addCompat(ContainerSkyChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, 36, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("draconicevolution")) {
//            api.addCompatSimple(ContainerDraconiumChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, 260, 26);
//            });
//            api.addPlayerSortButtonPosition(ContainerDraconiumChest.class, (slotGroup, buttonPos) -> {
//                ISlot topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
//                buttonPos.setVertical();
//                buttonPos.setTopLeft();
//                buttonPos.setPos(topRight.bogo$getX() + 17, topRight.bogo$getY() - 1);
//            });
//        }
//
//        if (Loader.isModLoaded("futuremc")) {
//            api.addCompatSimple(ContainerBarrel.class, (container, builder) -> {
//                builder.addSlotGroup(0, 27, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("projecte")) {
//            api.addCompat(CondenserContainer.class, (container, builder) -> {
//                builder.addSlotGroup(1, 92, 13);
//            });
//            api.addCompat(CondenserMK2Container.class, (container, builder) -> {
//                builder.addSlotGroup(1, 43, 6)
//                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//                builder.addSlotGroup(43, 85, 6)
//                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//            });
//            api.addPlayerSortButtonPosition(CondenserContainer.class, IPosSetter.TOP_RIGHT_VERTICAL);
//            api.addPlayerSortButtonPosition(CondenserMK2Container.class, IPosSetter.TOP_RIGHT_VERTICAL);
//        }
//
//        if (Loader.isModLoaded("immersiveengineering")) {
//            api.addCompat(ContainerCrate.class, (container, builder) -> {
//                builder.addSlotGroup(0, container.slotCount, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("forestry")) {
//            api.addCompat(ContainerBackpack.class, (container, builder) -> {
//                if (container.inventorySlots.size() == 51) {
//                    builder.addSlotGroup(36, container.inventorySlots.size(), 5);
//                } else {
//                    builder.addSlotGroup(36, container.inventorySlots.size(), 9);
//
//                }
//            });
//            api.addCompat(ContainerNaturalistBackpack.class, (container, builder) -> {
//                List<Slot> slots = new ArrayList<>();
//                for (int i = 0; i < 25; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        slots.add(container.getSlot(i * 5 + j + 36));
//                    }
//                }
//                builder.addSlotGroupOf(slots, 5)
//                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//            });
//            api.addCompat(ContainerNaturalistInventory.class, (container, builder) -> {
//                List<Slot> slots = new ArrayList<>();
//                for (int i = 0; i < 25; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        slots.add(container.getSlot(i * 5 + j + 36));
//                    }
//                }
//                builder.addSlotGroupOf(slots, 5)
//                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//            });
//            api.addPlayerSortButtonPosition(ContainerNaturalistBackpack.class, IPosSetter.TOP_RIGHT_VERTICAL);
//            api.addPlayerSortButtonPosition(ContainerNaturalistInventory.class, IPosSetter.TOP_RIGHT_VERTICAL);
//        }
//
//        if (BogoSorter.isIc2ExpLoaded()) {
//            api.addCompat(DynamicContainer.class, (container, builder) -> {
//                if (container.base instanceof TileEntityStorageBox) {
//                    if (container.base instanceof TileEntityWoodenStorageBox) {
//                        builder.addSlotGroup(0, 27, 9);
//                    } else if (container.base instanceof TileEntityBronzeStorageBox || container.base instanceof TileEntityIronStorageBox) {
//                        builder.addSlotGroup(0, 45, 9);
//                    } else if (container.base instanceof TileEntitySteelStorageBox) {
//                        builder.addSlotGroup(0, 63, 9);
//                    } else if (container.base instanceof TileEntityIridiumStorageBox) {
//                        builder.addSlotGroup(0, 126, 18);
//                    }
//                }
//                // personal safe client side
//                if (container.base instanceof TileEntityPersonalChest) {
//                    builder.addSlotGroup(0, 54, 9);
//                }
//            });
        // personal safe server side
//        api.addCompatSimple(getClass("ic2.core.block.personal.TileEntityPersonalChest$2"), (container, builder) -> {
//            builder.addSlotGroup(0, 54, 9);
//        });
//            api.addCompat(ContainerToolbox.class, (container, builder) -> {
//                builder.addSlotGroup(0, 9, 9);
//            });
//        }
//
//        if (BogoSorter.isIc2ClassicLoaded()) {
//            api.addCompat(ContainerPersonalChest.class, (container, builder) -> {
//                // make sure player can edit this chest
//                if (!(container.inventorySlots.get(0) instanceof SlotGhoest)) {
//                    builder.addSlotGroup(0, 54, 9);
//                }
//            });
//            api.addCompat(ContainerToolBox.class, (container, builder) -> {
//                ToolBoxInventory inv = container.getGuiHolder();
//                if (inv instanceof ToolBoxInventory.IridiumBoxInventory) {
//                    builder.addSlotGroup(0, 45, 9);
//                } else if (inv instanceof ToolBoxInventory.CarbonBoxInventory) {
//                    builder.addSlotGroup(0, 15, 5)
//                            .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//                } else {
//                    builder.addSlotGroup(0, 8, 4)
//                            .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//                }
//            });
//        }
//
//        if (Loader.isModLoaded("metalchests")) {
//            api.addCompat(ContainerMetalChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, container.type.getInventorySize(), container.type.getColumns());
//            });
//        }
//
//        if (BogoSorter.isGTCEuLoaded()) {
//            api.addCompat(ModularUIContainer.class, (container, builder) -> {
//                Map<String, List<Slot>> sortableSlots = new Object2ObjectOpenHashMap<>();
//
//                for (Widget widget : container.getModularUI().getFlatVisibleWidgetCollection()) {
//                    if (widget instanceof SortableSlotWidget) {
//                        SortableSlotWidget sortableSlotWidget = (SortableSlotWidget) widget;
//                        if (sortableSlotWidget.getSortArea() != null) {
//                            sortableSlots.computeIfAbsent(sortableSlotWidget.getSortArea(), (key) -> new ArrayList<>()).add(sortableSlotWidget.getHandle());
//                        }
//                    }
//                }
//
//                for (Map.Entry<String, List<Slot>> entry : sortableSlots.entrySet()) {
//                    int rowSize = ((IModularSortable) (Object) container.getModularUI()).getRowSize(entry.getKey());
//                    if (rowSize > 0) {
//                        builder.addSlotGroupOf(entry.getValue(), rowSize).buttonPosSetter(null);
//                    }
//                }
//            });
//            api.addPlayerSortButtonPosition(ModularUIContainer.class, null);
//        }
//
//        if (Loader.isModLoaded("travelersbackpack")) {
//            api.addCompat(ContainerTravelersBackpack.class, (container, builder) -> {
//                List<Slot> slots = new ArrayList<>();
//                for (int i = 0; i < 3; i++) {
//                    for (int j = 0; j < 8; j++) {
//                        slots.add(container.getSlot(i * 8 + j + 10));
//                    }
//                }
//                for (int i = 3; i < 6; i++) {
//                    for (int j = 0; j < 5; j++) {
//                        slots.add(container.getSlot((i - 3) * 5 + j + 34));
//                    }
//                }
//                builder.addSlotGroupOf(slots, 8);
//            });
//            api.addPlayerSortButtonPosition(ContainerTravelersBackpack.class, (slotGroup, buttonPos) -> {
//                ISlot topRight = slotGroup.getSlots().get(slotGroup.getRowSize() - 1);
//                buttonPos.setPos(topRight.bogo$getX() + 17, topRight.bogo$getY() - 1);
//            });
//        }
//
//        if (Loader.isModLoaded("colossalchests")) {
//            api.addCompat(ContainerColossalChest.class, (container, builder) -> {
//                List<Slot> chestSlots = ((ContainerColossalChestAccessor) container).getChestSlots();
//                builder.addSlotGroupOf(chestSlots, 9).buttonPosSetter((slotGroup, buttonPos) -> {
//                    buttonPos.setPos(0, 1000);
//                    for (ISlot slot : slotGroup.getSlots()) {
//                        if (slot.bogo$getX() >= 0 && slot.bogo$getY() >= 0 && slot.bogo$isEnabled()) {
//                            buttonPos.setPos(Math.max(buttonPos.getX(), slot.bogo$getX() + 17), Math.min(buttonPos.getY(), slot.bogo$getY() - 2));
//                        }
//                    }
//                });
//            });
//            api.addPlayerSortButtonPosition(ContainerColossalChest.class, (slotGroup, buttonPos) -> {
//                ISlot slot = slotGroup.getSlots().get(26);
//                buttonPos.setPos(slot.bogo$getX() + 19, slot.bogo$getY() - 2);
//                buttonPos.setTopLeft();
//                buttonPos.setVertical();
//            });
//            api.addCompat(ContainerUncolossalChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, 5, 5);
//            });
//        }
//
//        if (BogoSorter.isQuarkLoaded()) {
//            api.addCompat(vazkii.quark.oddities.inventory.ContainerBackpack.class, (container, builder) -> {
//                builder.addSlotGroup(46, 46 + 27, 9)
//                        .buttonPosSetter(IPosSetter.TOP_RIGHT_VERTICAL);
//            });
//            api.addPlayerSortButtonPosition(vazkii.quark.oddities.inventory.ContainerBackpack.class, IPosSetter.TOP_RIGHT_VERTICAL);
//        }
//
//        if (Loader.isModLoaded("cyclicmagic")) {
//            api.addCompat(ContainerStorage.class, (container, builder) -> {
//                builder.addSlotGroup(0, 77, 11);
//            });
//            api.addPlayerSortButtonPosition(ContainerStorage.class, (slotGroup, buttonPos) -> {
//                ISlot topRight = slotGroup.getSlots().get(26);
//                buttonPos.setVertical();
//                buttonPos.setTopLeft();
//                buttonPos.setPos(topRight.bogo$getX() + 18, topRight.bogo$getY() + 3);
//            });
//        }
//
//        if (Loader.isModLoaded("bibliocraft")) {
//            api.addCompat(ContainerFramedChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, container.getMainTile().getIsDouble() ? 27 * 2 : 27, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("railcraft")) {
//            api.addCompat(ContainerRCChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, container.getInv().getSizeInventory(), 9);
//            });
//        }
//
//        if (Loader.isModLoaded("energycontrol")) {
//            api.addCompat(ContainerCardHolder.class, (container, builder) -> {
//                builder.addSlotGroup(0, 54, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("projectred-exploration")) {
//            api.addCompat(mrtjp.projectred.exploration.ContainerBackpack.class, (container, builder) -> {
//                builder.addSlotGroup(0, 27, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("thebetweenlands")) {
//            api.addCompat(ContainerPouch.class, (container, builder) -> {
//                IInventory inventory = container.getItemInventory();
//                builder.addSlotGroup(0, inventory.getSizeInventory(), 9);
//            });
//        }
//
//        if (Loader.isModLoaded("tfc")) {
//            api.addCompat(ContainerChestTFC.class, (container, builder) -> {
//                builder.addSlotGroup(0, container.getLowerChestInventory().getSizeInventory(), 9);
//            });
//        }
//
//        if (Loader.isModLoaded("galacticraftcore")) {
//            api.addCompat(ContainerParaChest.class, (container, builder) -> {
//                int slot = container.getparachestInventory().getSizeInventory() - 3;
//
//                if (slot > 0) {
//                    builder.addSlotGroup(0, slot, 9);
//                }
//            });
//        }
//
//        if (Loader.isModLoaded("rustic")) {
//            api.addCompat(ContainerCabinet.class, (container, builder) -> {
//                builder.addGenericSlotGroup()
//                        .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL);
//            });
//            api.addCompat(ContainerCabinetDouble.class, (container, builder) -> {
//                builder.addGenericSlotGroup()
//                        .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL);
//            });
//            api.addCompat(rustic.common.tileentity.ContainerBarrel.class, (container, builder) -> builder.addGenericSlotGroup()
//                    .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL));
//            api.addCompat(ContainerVase.class, (container, builder) -> builder.addGenericSlotGroup()
//                    .buttonPosSetter(BogoSorter.isQuarkLoaded() ? IPosSetter.TOP_RIGHT_VERTICAL : IPosSetter.TOP_RIGHT_HORIZONTAL));
//        }
//
//        if (Loader.isModLoaded("avaritiaddons")) {
//            api.addSlotGetter(InfinitySlot.class, InfinitySlotWrapper::new);
//            api.addCustomInsertable(ContainerInfinityChest.class, (container, slots, itemStack, emptyOnly) -> {
//                ISlot slot = avaritiaddons$findSlot(slots, itemStack, emptyOnly);
//                if (slot == null) return itemStack;
//                if (emptyOnly) itemStack = ShortcutHandler.insert(slot, itemStack, true);
//                return itemStack.isEmpty() ? ItemStack.EMPTY : ShortcutHandler.insert(slot, itemStack, false);
//            });
//            api.addCompatSimple(ContainerCompressedChest.class, (container, builder) -> {
//                builder.addSlotGroup(0, 9 * 27, 27);
//            });
//            api.addCompatSimple(ContainerInfinityChest.class, (container, builder) -> {
//                List<ISlot> slots = new ArrayList<>();
//                for (Slot slot : builder.getContainer().inventorySlots) {
//                    if (slot instanceof InfinitySlot) {
//                        slots.add(new InfinitySlotWrapper((InfinitySlot) slot));
//                    }
//                }
//                builder.addSlotGroup(slots, 27);
//            });
//        }
//
//        if (BogoSorter.isExpandableInventoryLoaded()) {
//            // mark as sortable
//            api.addCompat(ContainerExpandedInventory.class, (container, builder) -> {
//            });
//        }
//
//        if (Loader.isModLoaded("charm")) {
//            api.addCompat(svenhjol.charm.crafting.container.ContainerBarrel.class, (container, builder) -> {
//                builder.addSlotGroup(0, 27, 9);
//            });
//        }
//
//        if (Loader.isModLoaded("industrialrenewal")) {
//            api.addCompat(ContainerStorageChest.class, (container, builder) -> {
//                builder.addGenericSlotGroup().buttonPosSetter((slotGroup, buttonPos) -> {
//                    buttonPos.setPos(0, 1000);
//                    for (ISlot slot : slotGroup.getSlots()) {
//                        if (slot.bogo$getX() >= 0 && slot.bogo$getY() >= 0 && slot.bogo$isEnabled()) {
//                            buttonPos.setPos(Math.max(buttonPos.getX(), slot.bogo$getX() + 17), Math.min(buttonPos.getY(), slot.bogo$getY() - 2));
//                        }
//                    }
//                });
//            });
//            api.addPlayerSortButtonPosition(ContainerStorageChest.class, (slotGroup, buttonPos) -> {
//                ISlot slot = slotGroup.getSlots().get(26);
//                buttonPos.setPos(slot.bogo$getX() + 19, slot.bogo$getY() - 2);
//                buttonPos.setTopLeft();
//                buttonPos.setVertical();
//            });
//        }
//
//        if (Loader.isModLoaded("cookingforblockheads")) {
//            api.addGenericCompat(ContainerCounter.class);
//            api.addGenericCompat(ContainerFridge.class);
//        }
//
//        if (Loader.isModLoaded("mekanism")) {
//            api.addGenericCompat(mekanism.common.inventory.container.ContainerPersonalChest.class);
//        }
//
//        if (Loader.isModLoaded("conarm")) {
//            api.addGenericCompat(ContainerKnapsack.class);
//        }
//    }
//
//    private static ISlot avaritiaddons$findSlot(List<ISlot> slots, ItemStack itemStack, boolean emptyOnly) {
//        for (ISlot slot : slots) {
//            ItemStack stackInSlot = slot.bogo$getStack();
//            if (!stackInSlot.isEmpty() && ItemHandlerHelper.canItemStacksStack(stackInSlot, itemStack)) {
//                return emptyOnly ? null : slot;
//            }
//        }
//        for (ISlot slot : slots) {
//            if (slot.bogo$getStack().isEmpty()) return slot;
//        }
//        return null;
//    }
//
//        public static Class<?> getClass1(String name){
//            try {
//                return Class.forName(name, false, DefaultCompat.class.getClassLoader());
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
    }
}
