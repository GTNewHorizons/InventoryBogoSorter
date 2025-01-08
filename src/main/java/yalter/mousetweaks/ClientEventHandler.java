package yalter.mousetweaks;

import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import com.cleanroommc.modularui.api.event.MouseInputEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ClientEventHandler {

    private static final Set<Class<? extends GuiScreen>> mouseTweaksBlacklist = new ObjectOpenHashSet<>();
    private static final Set<Class<? extends GuiScreen>> wheelTweaksBlacklist = new ObjectOpenHashSet<>();

    public static void addMouseTweakBlacklist(Class<? extends GuiScreen> clazz) {
        mouseTweaksBlacklist.add(clazz);
    }

    public static void addWheelTweaksBlacklist(Class<? extends GuiScreen> clazz) {
        wheelTweaksBlacklist.add(clazz);
    }

    public static boolean isMouseTweakDisabled(Class<? extends GuiScreen> clazz) {
        return mouseTweaksBlacklist.contains(clazz);
    }

    public static boolean isWheelTweakDisabled(Class<? extends GuiScreen> clazz) {
        return wheelTweaksBlacklist.contains(clazz);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Main.onUpdateInGame();
        }
    }

    @SubscribeEvent
    public void onGuiMouseInput(MouseInputEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            Main.onMouseInput();
        }
    }
}
