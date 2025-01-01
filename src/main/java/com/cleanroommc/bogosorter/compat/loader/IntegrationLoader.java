package com.cleanroommc.bogosorter.compat.loader;


import java.util.Arrays;

public class IntegrationLoader {

    public static void load() {
        Arrays.stream(Mods.values()).forEach(Mods::isLoaded);
    }
}
