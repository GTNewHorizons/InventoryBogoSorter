package com.cleanroommc.bogosorter.compat;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.cubefury.vendingmachine.blocks.MTEVendingMachine;
import com.cubefury.vendingmachine.blocks.gui.WalletMode;
import com.cubefury.vendingmachine.trade.CurrencyItem;
import com.cubefury.vendingmachine.trade.TradeManager;
import com.cubefury.vendingmachine.util.Wallet;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public final class VendingMachineCompat {

    private static final int PLAYER_INVENTORY_FIRST = 9;

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

            CurrencyItem currency = CurrencyItem.fromItemStack(stack);
            if (currency == null) {
                continue;
            }

            itemsDeposited += stack.stackSize;
            playerStacks[slot] = null;
            wallet.addCount(currency.type, currency.value);
        }

        if (itemsDeposited > 0) {
            TradeManager.INSTANCE.saveTeamData(player.getUniqueID());
            vendingMachine.playSoundEffect("vendingmachine:coin_insert");
        }
        return itemsDeposited;
    }

    private static MTEVendingMachine getVendingMachine(IInventory inventory) {
        if (!(inventory instanceof IGregTechTileEntity gregTechTile)) {
            return null;
        }

        IMetaTileEntity metaTileEntity = gregTechTile.getMetaTileEntity();
        return metaTileEntity instanceof MTEVendingMachine vendingMachine ? vendingMachine : null;
    }
}
