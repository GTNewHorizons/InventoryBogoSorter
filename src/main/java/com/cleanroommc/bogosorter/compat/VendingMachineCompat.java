package com.cleanroommc.bogosorter.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S29PacketSoundEffect;

import com.cubefury.vendingmachine.blocks.MTEVendingMachine;
import com.cubefury.vendingmachine.blocks.gui.WalletMode;
import com.cubefury.vendingmachine.trade.CurrencyItem;
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.Wallet;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public final class VendingMachineCompat {

    private static final int PLAYER_INVENTORY_FIRST = 9;
    private static final String DROPOFF_SOUND = "bogosorter:vending_dropoff";

    private VendingMachineCompat() {}

    public static boolean isVendingMachine(IInventory inventory) {
        return getVendingMachine(inventory) != null;
    }

    public static int depositCurrency(EntityPlayerMP player, IInventory inventory, int ignoredSlot) {
        MTEVendingMachine vendingMachine = getVendingMachine(inventory);
        if (vendingMachine == null || !vendingMachine.getActive()) {
            return 0;
        }

        Wallet wallet = TradeManager.INSTANCE.getWallet(player.getUniqueID(), WalletMode.PERSONAL);
        if (wallet == null) {
            return 0;
        }

        int itemsDeposited = 0;
        ItemStack[] playerStacks = player.inventory.mainInventory;
        for (int slot = PLAYER_INVENTORY_FIRST; slot < playerStacks.length; slot++) {
            ItemStack stack = playerStacks[slot];
            if (slot == ignoredSlot || stack == null) {
                continue;
            }

            CurrencyItem currency = getAcceptedCurrency(stack);
            if (currency == null) {
                continue;
            }

            itemsDeposited += stack.stackSize;
            playerStacks[slot] = null;
            wallet.addCount(currency.type, currency.value);
        }

        if (itemsDeposited > 0) {
            TradeManager.INSTANCE.saveTeamData(player.getUniqueID());
            playDropOffSound(player, vendingMachine);
        }
        return itemsDeposited;
    }

    private static CurrencyItem getAcceptedCurrency(ItemStack stack) {
        CurrencyItem currency = CurrencyItem.fromItemStack(stack);
        return currency != null && currency.type != null && currency.value > 0 ? currency : null;
    }

    private static void playDropOffSound(EntityPlayerMP player, MTEVendingMachine vendingMachine) {
        IGregTechTileEntity baseTile = vendingMachine.getBaseMetaTileEntity();
        if (baseTile == null) {
            return;
        }

        player.playerNetServerHandler.sendPacket(
            new S29PacketSoundEffect(
                DROPOFF_SOUND,
                baseTile.getXCoord() + 0.5,
                baseTile.getYCoord() + 0.5,
                baseTile.getZCoord() + 0.5,
                1.0F,
                1.0F));
    }

    private static MTEVendingMachine getVendingMachine(IInventory inventory) {
        if (!(inventory instanceof IGregTechTileEntity gregTechTile)) {
            return null;
        }

        IMetaTileEntity metaTileEntity = gregTechTile.getMetaTileEntity();
        return metaTileEntity instanceof MTEVendingMachine vendingMachine ? vendingMachine : null;
    }
}
