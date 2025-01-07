package yalter.mousetweaks.util;

import org.apache.logging.log4j.Logger;

public class MTLog {

    public static Logger logger;

    public static void init(Logger modLogger) {
        logger = modLogger;
    }
}
