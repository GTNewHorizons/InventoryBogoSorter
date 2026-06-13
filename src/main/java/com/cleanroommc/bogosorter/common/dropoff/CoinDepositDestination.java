package com.cleanroommc.bogosorter.common.dropoff;

public enum CoinDepositDestination {

    USER,
    TEAM;

    public CoinDepositDestination toggle() {
        return this == USER ? TEAM : USER;
    }
}
