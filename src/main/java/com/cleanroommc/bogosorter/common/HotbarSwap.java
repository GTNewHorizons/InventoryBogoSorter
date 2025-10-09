package com.cleanroommc.bogosorter.common;

import static org.lwjgl.opengl.GL11.GL_BLEND;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.cleanroommc.bogosorter.common.config.BogoSorterConfig;
import com.cleanroommc.bogosorter.common.network.CHotbarSwap;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

public class HotbarSwap {

    private static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");

    private static boolean show;
    private static int verticalIndex = 0;

    public static boolean doCancelHotbarSwap() {
        return show;
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (BogoSorterConfig.enableHotbarSwap && Minecraft.getMinecraft().thePlayer.inventory.currentItem < 9
            && event.type == RenderGameOverlayEvent.ElementType.ALL
            && show) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
            int m = event.resolution.getScaledWidth() / 2;
            if (verticalIndex != 0) {
                Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(WIDGETS_TEX_PATH);
                gui.drawTexturedModalRect(
                    m - 91 - 1 + player.inventory.currentItem * 20,
                    event.resolution.getScaledHeight() - 22 - 17 - 18 * verticalIndex,
                    0,
                    22,
                    24,
                    24);
            }

            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL_BLEND);

            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            int x = m - 90 + player.inventory.currentItem * 20 + 2;
            int y = event.resolution.getScaledHeight() - 16 - 3 - 70;
            for (int i = 1; i < 4; i++) {
                renderHotbarItem(
                    Minecraft.getMinecraft().fontRenderer,
                    Minecraft.getMinecraft()
                        .getTextureManager(),
                    player.inventory.getStackInSlot(player.inventory.currentItem + i * 9),
                    x,
                    y,
                    event.partialTicks);
                y += 18;
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL_BLEND);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!BogoSorterConfig.enableHotbarSwap || Minecraft.getMinecraft().theWorld == null
            || Minecraft.getMinecraft().thePlayer == null
            || Minecraft.getMinecraft().thePlayer.inventory.currentItem > 8) {
            return;
        }
        if (show) {
            if (!isAltKeyDown()) {
                // swap items on server
                if (verticalIndex != 0) {
                    int index = 4 - verticalIndex;
                    int current = Minecraft.getMinecraft().thePlayer.inventory.currentItem;
                    NetworkHandler.sendToServer(new CHotbarSwap(current, current + index * 9));
                }

                show = false;
                verticalIndex = 0;
            }
        } else {
            if (isAltKeyDown()) {
                show = true;
                verticalIndex = 0;
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (!BogoSorterConfig.enableHotbarSwap || Minecraft.getMinecraft().thePlayer.inventory.currentItem > 8) return;
        if (show) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                scroll = MathHelper.clamp_int(scroll, -1, 1);
                verticalIndex += scroll;
                if (verticalIndex > 3) {
                    verticalIndex = 0;
                } else if (verticalIndex < 0) {
                    verticalIndex = 3;
                }
            }
        }
    }

    private static void renderHotbarItem(FontRenderer fontRenderer, TextureManager textureManager, ItemStack stack,
        int x, int y, float partialTicks) {
        if (stack != null) {
            RenderItem renderer = new RenderItem();
            float f = (float) stack.animationsToGo - partialTicks;
            if (f > 0.0F) {
                GL11.glPushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GL11.glTranslatef((float) (x + 8), (float) (y + 12), 0.0F);
                GL11.glScalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }

            renderer.renderItemAndEffectIntoGUI(fontRenderer, textureManager, stack, x, y);

            if (f > 0.0F) {
                GL11.glPopMatrix();
            }

            renderer.renderItemOverlayIntoGUI(fontRenderer, textureManager, stack, x, y);
        }
    }

    public static boolean isAltKeyDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }
}
