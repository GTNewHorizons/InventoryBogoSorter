package com.cleanroommc.bogosorter.mixins.early.rftools;// package com.cleanroommc.bogosorter.core.mixin.early.rftools;
//
// import com.cleanroommc.bogosorter.common.sort.SortHandler;
// import mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
// import net.minecraft.item.ItemStack;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Redirect;
//
// import java.util.Comparator;
// import java.util.function.Function;
//
// @Mixin(value = GuiStorageScanner.class, remap = false)
// public class GuiStorageScannerMixin {
//
// @Redirect(method = "updateContentsList",
// at = @At(
// value = "INVOKE",
// target = "Ljava/util/Comparator;comparing(Ljava/util/function/Function;)Ljava/util/Comparator;",
// ordinal = 0
// ))
// public Comparator<ItemStack> redirectComparator(Function<ItemStack, String> no_one_likes_alphabetical_order) {
// return SortHandler.getClientItemComparator();
// }
// }
