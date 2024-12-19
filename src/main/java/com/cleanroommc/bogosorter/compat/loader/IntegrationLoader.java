package com.cleanroommc.bogosorter.compat.loader;


import java.util.Arrays;

public class IntegrationLoader {

    public static final IntegrationLoader INSTANCE = new IntegrationLoader();

    public void load() {
        Arrays.stream(Mods.values()).forEach(Mods::check);
    }
}
