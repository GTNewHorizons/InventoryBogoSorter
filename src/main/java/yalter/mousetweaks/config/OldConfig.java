package yalter.mousetweaks.config;


import yalter.mousetweaks.util.MTLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class OldConfig {

    public static void handleOldConfig(File file) {
        if (!file.exists()) return;

        try {
            if (Files.readAllLines(file.toPath()).stream().noneMatch(str -> str.contains("general"))) {
                file.delete();
            }
        } catch (IOException e) {
            MTLog.logger.error(e);
        }
    }

}
