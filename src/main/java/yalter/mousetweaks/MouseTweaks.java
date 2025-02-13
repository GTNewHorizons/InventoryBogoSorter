package yalter.mousetweaks;

import static yalter.mousetweaks.ClientEventHandler.addMouseTweakBlacklist;
import static yalter.mousetweaks.ClientEventHandler.addWheelTweaksBlacklist;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import moze_intel.projecte.gameObjs.gui.GUICondenser;
import moze_intel.projecte.gameObjs.gui.GUICondenserMK2;
import yalter.mousetweaks.config.MTConfig;
import yalter.mousetweaks.config.OldConfig;

@Mod(
    modid = MouseTweaks.MOD_ID,
    name = MouseTweaks.MOD_NAME,
    version = BogoSorter.VERSION,
    dependencies = "required-after:gtnhlib@[0.6.1,);",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*")
public class MouseTweaks {

    public static final String MOD_NAME = "Mouse Tweaks Unofficial";
    public static final String MOD_ID = "MouseTweaks";
    public static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            try {
                OldConfig.handleOldConfig(event.getSuggestedConfigurationFile());
                ConfigurationManager.registerConfig(MTConfig.class);
            } catch (ConfigException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            if (Loader.isModLoaded("projecte")) {
                addMouseTweakBlacklist(GUICondenser.class);
                addWheelTweaksBlacklist(GUICondenser.class);
                addMouseTweakBlacklist(GUICondenserMK2.class);
                addWheelTweaksBlacklist(GUICondenserMK2.class);
            }
            Main.initialize();
            FMLCommonHandler.instance()
                .bus()
                .register(new ClientEventHandler());
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        }
    }
}
