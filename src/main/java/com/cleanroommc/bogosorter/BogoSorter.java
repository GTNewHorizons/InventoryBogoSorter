package com.cleanroommc.bogosorter;

import java.time.LocalDate;
import java.time.Month;

import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cleanroommc.bogosorter.client.keybinds.control.BSKeybinds;
import com.cleanroommc.bogosorter.common.HotbarSwap;
import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.SortConfigChangeEvent;
import com.cleanroommc.bogosorter.common.XSTR;
import com.cleanroommc.bogosorter.common.config.BogoSortCommandTree;
import com.cleanroommc.bogosorter.common.config.Serializer;
import com.cleanroommc.bogosorter.common.dropoff.DropOffButtonHandler;
import com.cleanroommc.bogosorter.common.dropoff.DropOffScheduler;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import com.cleanroommc.bogosorter.common.network.NetworkUtils;
import com.cleanroommc.bogosorter.common.refill.RefillHandler;
import com.cleanroommc.bogosorter.common.sort.ButtonHandler;
import com.cleanroommc.bogosorter.common.sort.DefaultRules;
import com.cleanroommc.bogosorter.compat.DefaultCompat;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

@Mod(
    modid = BogoSorter.ID,
    name = BogoSorter.NAME,
    version = BogoSorter.VERSION,
    dependencies = "required-after:unimixins@[0.1.19,);" + "required-after:gtnhlib@[0.6.1,);"
        + "required-after:modularui2@[2.2.2,);")
public class BogoSorter {

    public static final String ID = "bogosorter";
    public static final String NAME = "Inventory Bogo Sorter";
    public static final String VERSION = Tags.VERSION;

    public static final XSTR RND = new XSTR();
    public static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        NetworkHandler.init();
        OreDictHelper.init();
        BogoSortAPI.INSTANCE.remapSortRule("is_block", "block_type");
        DefaultRules.init(BogoSortAPI.INSTANCE);
        DefaultCompat.init(BogoSortAPI.INSTANCE);
        Serializer.loadConfig();
        MinecraftForge.EVENT_BUS.register(new RefillHandler());
        FMLCommonHandler.instance()
            .bus()
            .register(DropOffScheduler.INSTANCE);
        if (NetworkUtils.isDedicatedClient()) {
            MinecraftForge.EVENT_BUS.post(new SortConfigChangeEvent());
            FMLCommonHandler.instance()
                .bus()
                .register(new ClientEventHandler());
            MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
            MinecraftForge.EVENT_BUS.register(new DropOffButtonHandler());
            MinecraftForge.EVENT_BUS.register(new ButtonHandler());
            FMLCommonHandler.instance()
                .bus()
                .register(new HotbarSwap());
            MinecraftForge.EVENT_BUS.register(new HotbarSwap());
            BSKeybinds.init(event.getSuggestedConfigurationFile());
        }
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        if (NetworkUtils.isDedicatedClient()) {
            ClientRegistry.registerKeyBinding(BSKeybinds.configGuiKey);
            ClientRegistry.registerKeyBinding(BSKeybinds.sortKeyOutsideGUI);
            ClientRegistry.registerKeyBinding(BSKeybinds.sortKeyInGUI);
            ClientRegistry.registerKeyBinding(BSKeybinds.dropoffKey);
            ClientRegistry.registerKeyBinding(BSKeybinds.BOGO_SORTER_CONTROLS_BUTTON);
        }
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new BogoSortCommandTree());
    }

    @SubscribeEvent
    public void onPlayerLogout(FMLNetworkEvent.ClientDisconnectionFromServerEvent ignored) {
        // save config file on logout
        Serializer.saveConfig();
    }

    public static boolean isAprilFools() {
        LocalDate date = LocalDate.now();
        return date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1;
    }
}
