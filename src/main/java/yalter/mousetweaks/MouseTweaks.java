package yalter.mousetweaks;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.modularui.api.event.MouseInputEvent;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import moze_intel.projecte.gameObjs.gui.GUICondenser;
import moze_intel.projecte.gameObjs.gui.GUICondenserMK2;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.MinecraftForge;

import yalter.mousetweaks.config.MTConfig;
import yalter.mousetweaks.config.OldConfig;
import yalter.mousetweaks.util.MTLog;

import java.util.Set;


@Mod(
        modid = MouseTweaks.MOD_ID,
        name = MouseTweaks.MOD_NAME,
        version = BogoSorter.VERSION,
        dependencies = "required-after:gtnhlib@[0.5.16,);",
        acceptedMinecraftVersions = "[1.7.10]",
        acceptableRemoteVersions = "*")
public class MouseTweaks {

    public static final String MOD_NAME = "Mouse Tweaks";
    public static final String MOD_ID = "MouseTweaks";

    private static final Set<Class<? extends GuiScreen>> mouseTweaksBlacklist = new ObjectOpenHashSet<>();
    private static final Set<Class<? extends GuiScreen>> wheelTweaksBlacklist = new ObjectOpenHashSet<>();

    public void addMouseTweakBlacklist(Class<? extends GuiScreen> clazz) {
        this.mouseTweaksBlacklist.add(clazz);
    }

    public void addWheelTweaksBlacklist(Class<? extends GuiScreen> clazz) {
        this.wheelTweaksBlacklist.add(clazz);
    }

    public static boolean isMouseTweakDisabled(Class<? extends GuiScreen> clazz) {
        return mouseTweaksBlacklist.contains(clazz);
    }

    public static boolean isWheelTweakDisabled(Class<? extends GuiScreen> clazz) {
        return wheelTweaksBlacklist.contains(clazz);
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        MTLog.init(event.getModLog());
        if (event.getSide().isClient()) {
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
        if (event.getSide() != Side.CLIENT) {
            MTLog.logger.info("MouseTweaks disabled because we are not running on the client");
            return;
        }
        if (Loader.isModLoaded("projecte")) {
            mouseTweaksBlacklist.add(GUICondenser.class);
            wheelTweaksBlacklist.add(GUICondenser.class);
            mouseTweaksBlacklist.add(GUICondenserMK2.class);
            wheelTweaksBlacklist.add(GUICondenserMK2.class);
        }
//        if (Loader.isModLoaded("packagedauto")) {
//            mouseTweaksBlacklist.add(GuiEncoder.class);
//            wheelTweaksBlacklist.add(GuiEncoder.class);
//        }
        Main.initialize();
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Main.onUpdateInGame();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onGuiMouseInput(MouseInputEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            Main.onMouseInput();
        }
    }
}
