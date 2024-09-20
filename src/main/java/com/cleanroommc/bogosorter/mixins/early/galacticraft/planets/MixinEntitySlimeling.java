package com.cleanroommc.bogosorter.mixins.early.galacticraft.planets;// package com.cleanroommc.bogosorter.core.mixin.early.galacticraft.planets;
//
// import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
// import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
// import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
// import micdoodle8.mods.galacticraft.planets.mars.MarsModuleClient;
// import micdoodle8.mods.galacticraft.planets.mars.entities.EntitySlimeling;
// import net.minecraft.entity.EntityLivingBase;
// import net.minecraft.entity.passive.EntityTameable;
// import net.minecraft.entity.player.EntityPlayer;
// import net.minecraft.entity.player.EntityPlayerMP;
// import net.minecraft.init.Items;
// import net.minecraft.item.Item;
// import net.minecraft.item.ItemStack;
// import net.minecraft.util.EnumHand;
// import net.minecraft.util.text.TextComponentString;
// import net.minecraft.world.World;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.Shadow;
//
// @Mixin(EntitySlimeling.class)
// public abstract class MixinEntitySlimeling extends EntityTameable implements IEntityBreathable {
//
// @Shadow(remap = false)
// public abstract void setSittingAI(boolean sitting);
//
// @Shadow(remap = false)
// public abstract void setOwnerUsername(String username);
//
// @Shadow(remap = false)
// protected abstract void setRandomFavFood();
//
// @Shadow(remap = false)
// public abstract Item getFavoriteFood();
//
// public MixinEntitySlimeling(World worldIn) {
// super(worldIn);
// }
//
// @Override
// public boolean processInteract(EntityPlayer player, EnumHand hand) {
// ItemStack itemstack = player.inventory.getCurrentItem();
// EntitySlimeling entity = (EntitySlimeling) (Object) this;
//
// if (this.isTamed()) {
// if (!itemstack.isEmpty()) {
// if (itemstack.getItem() == this.getFavoriteFood()) {
// if (this.isOwner(player)) {
// if (!this.world.isRemote) {
// itemstack.shrink(1);
// if (itemstack.isEmpty()) {
// player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
// }
// }
//
// if (this.world.isRemote) {
// MarsModuleClient.openSlimelingGui(entity, 1);
// }
//
// if (this.rand.nextInt(3) == 0) {
// this.setRandomFavFood();
// }
// } else if (player instanceof EntityPlayerMP) {
// GCPlayerStats stats = GCPlayerStats.get(player);
// if (stats.getChatCooldown() == 0) {
// player.sendMessage(new TextComponentString(GCCoreUtil.translate("gui.slimeling.chat.wrong_player")));
// stats.setChatCooldown(100);
// }
// }
// } else if (this.world.isRemote) {
// MarsModuleClient.openSlimelingGui(entity, 0);
// }
// } else if (this.world.isRemote) {
// MarsModuleClient.openSlimelingGui(entity, 0);
// }
//
// return true;
// } else if (!itemstack.isEmpty() && itemstack.getItem() == Items.SLIME_BALL) {
// if (!player.capabilities.isCreativeMode) {
// itemstack.shrink(1);
// }
//
// if (itemstack.isEmpty()) {
// player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
// }
//
// if (!this.world.isRemote) {
// if (this.rand.nextInt(3) == 0) {
// this.setTamed(true);
// this.getNavigator().clearPath();
// this.setAttackTarget((EntityLivingBase) null);
// this.setSittingAI(true);
// this.setHealth(20.0F);
// this.setOwnerId(player.getUniqueID());
// this.setOwnerUsername(player.getName());
// this.playTameEffect(true);
// this.world.setEntityState(this, (byte) 7);
// } else {
// this.playTameEffect(false);
// this.world.setEntityState(this, (byte) 6);
// }
// }
//
// return true;
// } else {
// return super.processInteract(player, hand);
// }
// }
//
// }
