package com.cleanroommc.bogosorter.common.network.ae2;

public final class Ae2Status {

    public static final int OK = 0;
    public static final int NO_SYSTEM = 1;
    public static final int THROTTLED = 2;
    public static final int ERROR = 3;
    public static final int OUT_OF_RANGE = 4;
    public static final int UNSUPPORTED = 5;

    private Ae2Status() {}

    public static boolean isValid(int status) {
        return status >= OK && status <= UNSUPPORTED;
    }
}
