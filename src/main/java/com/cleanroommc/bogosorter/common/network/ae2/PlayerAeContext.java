package com.cleanroommc.bogosorter.common.network.ae2;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record PlayerAeContext(Object host, Object grid, Object configManager) {

    public Object cacheOwner() {
        return grid == null ? host : grid;
    }
}
