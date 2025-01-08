package yalter.mousetweaks.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import yalter.mousetweaks.MouseTweaks;

public class OldConfig {

    public static void handleOldConfig(File file) {
        if (!file.exists()) return;

        try {
            if (Files.readAllLines(file.toPath())
                .stream()
                .noneMatch(str -> str.contains("general"))) {
                file.delete();
            }
        } catch (IOException e) {
            MouseTweaks.LOGGER.error(e);
        }
    }

}
