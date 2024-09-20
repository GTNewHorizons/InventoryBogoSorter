package com.cleanroommc.bogosorter.mixins.early.enderio;// package com.cleanroommc.bogosorter.core.mixin.early.enderio;
//
// import com.cleanroommc.bogosorter.api.IPosSetter;
// import com.cleanroommc.bogosorter.api.ISortableContainer;
// import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
// import crazypants.enderio.base.item.darksteel.upgrade.storage.StorageCap;
// import crazypants.enderio.base.item.darksteel.upgrade.storage.StorageContainer;
// import crazypants.enderio.util.EIOCombinedInvWrapper;
// import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
// import it.unimi.dsi.fastutil.objects.ObjectArrayList;
// import net.minecraft.inventory.Container;
// import net.minecraft.inventory.EntityEquipmentSlot;
// import net.minecraft.inventory.Slot;
// import org.spongepowered.asm.mixin.Mixin;
//
// import java.util.List;
// import java.util.Map;
//
// @Mixin(StorageContainer.class)
// public class MixinEIOStorage implements ISortableContainer {
//
// @Override
// public void buildSortingContext(ISortingContextBuilder builder) {
// Map<EntityEquipmentSlot, List<Slot>> slots = new Object2ObjectOpenHashMap<>();
// EIOCombinedInvWrapper<StorageCap> itemHandler = ((StorageContainer) (Object) this).getItemHandler();
// for (int i = 0; i < itemHandler.getSlots(); i++) {
// EntityEquipmentSlot current = itemHandler.getHandlerFromSlot(i).getEquipmentSlot();
// Slot slot = ((Container) (Object) this).inventorySlots.get(i);
// slots.computeIfAbsent(current, key -> new ObjectArrayList<>()).add(slot);
// }
// slots.forEach((slot, slotList) -> builder.addSlotGroupOf(slotList, StorageUpgradeAccessor.invokeCols(slot))
// .buttonPosSetter((slotGroup, buttonPos) -> {
// IPosSetter.TOP_RIGHT_HORIZONTAL.setButtonPos(slotGroup, buttonPos);
// buttonPos.setEnabled(slotGroup.getSlots().get(0).bogo$isEnabled());
// }));
// }
// }
