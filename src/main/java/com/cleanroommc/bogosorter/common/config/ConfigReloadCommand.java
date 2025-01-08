package com.cleanroommc.bogosorter.common.config;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.SReloadConfig;

public class ConfigReloadCommand extends CommandBase {

    @Override
    public @NotNull String getCommandName() {
        return "reload";
    }

    @Override
    public @NotNull String getCommandUsage(@NotNull ICommandSender sender) {
        return "/bogosort reload";
    }

    @Override
    public void processCommand(@NotNull ICommandSender sender, String @NotNull [] args) {
        if (sender instanceof EntityPlayerMP) {
            NetworkHandler.sendToPlayer(new SReloadConfig(), (EntityPlayerMP) sender);
            sender.addChatMessage(new ChatComponentTranslation("bogosort.command.config_relaod.success"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
