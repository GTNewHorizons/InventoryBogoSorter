package com.cleanroommc.bogosorter.common.network.ae2;

import net.minecraft.entity.player.EntityPlayerMP;

import com.cleanroommc.bogosorter.BogoSorter;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.ISecurityGrid;

final class Ae2AccessHelper {

    private static boolean loggedSecurityFailure;

    private Ae2AccessHelper() {}

    static boolean canPlayerReadGrid(EntityPlayerMP player, IGrid grid) {
        if (player == null || grid == null) {
            return false;
        }

        try {
            ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
            return securityGrid == null || !securityGrid.isAvailable()
                || securityGrid.hasPermission(player, SecurityPermissions.EXTRACT);
        } catch (RuntimeException | LinkageError e) {
            if (!loggedSecurityFailure) {
                loggedSecurityFailure = true;
                BogoSorter.LOGGER.error("AE2 security permission check failed; denying remote storage access", e);
            }
            return false;
        }
    }
}
