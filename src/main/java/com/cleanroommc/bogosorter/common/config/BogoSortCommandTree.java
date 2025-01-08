package com.cleanroommc.bogosorter.common.config;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.common.CommandTreeBase;
import com.cleanroommc.bogosorter.common.OreDictHelper;

public class BogoSortCommandTree extends CommandTreeBase {

    public BogoSortCommandTree() {
        addSubcommand(new ConfigReloadCommand());
        addSubcommand(new CommandBase() {

            @Override
            public String getCommandName() {
                return "hand";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "/bogosorter hand";
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) throws CommandException {
                if (sender instanceof EntityPlayer) {
                    ItemStack itemStack = ((EntityPlayer) sender).getHeldItem();
                    if (itemStack == null) itemStack = ((EntityPlayer) sender).getHeldItem();
                    if (itemStack == null) return;
                    String material = OreDictHelper.getMaterial(itemStack);
                    String prefix = OreDictHelper.getOrePrefix(itemStack);
                    sender.addChatMessage(new ChatComponentText("Material:  " + material));
                    sender.addChatMessage(new ChatComponentText("OrePrefix: " + prefix));
                }
            }
        });
    }

    @Override
    public @NotNull String getCommandName() {
        return "bogosorter";
    }

    @Override
    public @NotNull String getCommandUsage(@NotNull ICommandSender sender) {
        return "/bogosorter [reload]";
    }
}
