package com.cleanroommc.bogosorter.common.config.ae2;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.ae2.Ae2AmountService;
import com.cleanroommc.bogosorter.common.network.ae2.STooltipFeatureState;
import com.cleanroommc.bogosorter.compat.Mods;

public class TooltipCommand extends CommandBase {

    @Override
    public @NotNull String getCommandName() {
        return "tooltip";
    }

    @Override
    public @NotNull String getCommandUsage(@NotNull ICommandSender sender) {
        // so ranzu doesn't come after me
        return StatCollector.translateToLocal("bogosorter.command.tooltip.usage");
    }

    @Override
    public void processCommand(@NotNull ICommandSender sender, String @NotNull [] args) throws CommandException {
        if (args.length != 1 || (!"on".equalsIgnoreCase(args[0]) && !"off".equalsIgnoreCase(args[0]))) {
            throw new CommandException("bogosorter.command.tooltip.usage");
        }

        boolean enabled = "on".equalsIgnoreCase(args[0]);
        TooltipFeatureConfig.setTooltipEnabled(enabled);
        TooltipFeatureConfig.save();
        Ae2AmountService.clearCaches();
        // only broadcast tooltip state when ae2 is loaded
        if (Mods.Ae2.isLoaded()) {
            NetworkHandler.sendToAll(
                new STooltipFeatureState(
                    TooltipFeatureConfig.isAmountTooltipEnabled(),
                    TooltipFeatureConfig.isThaumicEnabled()));
        }
        sender.addChatMessage(
            new ChatComponentTranslation(
                enabled ? "bogosorter.command.tooltip.enabled" : "bogosorter.command.tooltip.disabled"));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "on", "off");
        }
        return Collections.emptyList();
    }
}
