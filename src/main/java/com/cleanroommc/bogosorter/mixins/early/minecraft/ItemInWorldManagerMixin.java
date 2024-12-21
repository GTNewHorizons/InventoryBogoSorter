package com.cleanroommc.bogosorter.mixins.early.minecraft;



import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemInWorldManager.class)
public class ItemInWorldManagerMixin {

    @Shadow
    public EntityPlayerMP thisPlayerMP;

    @Inject(
        method = "tryUseItem",
        at = @At(
            value = "INVOKE",
            target = "net/minecraftforge/event/entity/player/PlayerDestroyItemEvent.<init>(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V", // The target method to inject after
            shift = At.Shift.BY, by = 2
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void tryUseItem(EntityPlayer p_73085_1_, World p_73085_2_, ItemStack p_73085_3_, CallbackInfoReturnable<Boolean> cir, int i, int j, ItemStack itemstack1) {
        RefillHandler.onDestroyItem(this.thisPlayerMP, itemstack1);
    }


    @Inject(
        method = "activateBlockOrUseItem",
        at = @At(
            value = "INVOKE",
            target = "net/minecraftforge/event/ForgeEventFactory.onPlayerDestroyItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V", // The target method to inject after
            shift = At.Shift.AFTER
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        remap = false
    )
    private void activateBlockOrUseItem(EntityPlayer player, World worldIn, ItemStack stack, int p_73078_4_, int p_73078_5_, int p_73078_6_, int p_73078_7_, float p_73078_8_, float p_73078_9_, float p_73078_10_, CallbackInfoReturnable<Boolean> cir, PlayerInteractEvent event) {
         RefillHandler.onDestroyItem(this.thisPlayerMP, stack);
    }
}

