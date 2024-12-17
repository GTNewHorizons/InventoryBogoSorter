package com.cleanroommc.bogosorter.common;

import com.cleanroommc.bogosorter.common.network.CHotbarSwap;
import com.cleanroommc.bogosorter.common.network.NetworkHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
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

public class HotbarSwap {

    private static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");

    private static boolean enabled = true;
    private static boolean show;
    private static int verticalIndex = 0;
    protected static final RenderItem itemRenderer = RenderItem.getInstance();
    private static final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
    private static final TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

    public static boolean doCancelHotbarSwap() {
        return show;
    }

    public static void setEnabled(boolean enabled) {
        HotbarSwap.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Post event) {
        if (enabled && event.type == RenderGameOverlayEvent.ElementType.ALL && show) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
            int m = event.resolution.getScaledWidth() / 2;
            if (verticalIndex != 0) {
                Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_TEX_PATH);
                gui.drawTexturedModalRect(m - 91 - 1 + player.inventory.currentItem * 20, event.resolution.getScaledHeight() - 22 - 17 - 18 * verticalIndex, 0, 22, 24, 22);
            }

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();

            int x = m - 90 + player.inventory.currentItem * 20 + 2;
            int y = event.resolution.getScaledHeight() - 16 - 3 - 70;
            for (int i = 1; i < 4; i++) {
                renderHotbarItem(x, y, event.partialTicks, player, player.inventory.getStackInSlot(player.inventory.currentItem + i * 9));
                y += 18;
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!enabled || Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) {
            return;
        }
        if (show) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LMENU) || !Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
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
            if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
                show = true;
                verticalIndex = 0;
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(InputEvent.MouseInputEvent event) {
        if (!enabled) return;
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

    private static void renderHotbarItem(int x, int y, float partialTicks, EntityPlayer player, ItemStack stack) {
        if (stack != null) {
            float f = (float) stack.animationsToGo - partialTicks;

            if (f > 0.0F) {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float) (x + 8), (float) (y + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, textureManager,stack, x, y);

            if (f > 0.0F) {
                GlStateManager.popMatrix();
            }

            itemRenderer.renderItemOverlayIntoGUI(fontRenderer, textureManager, stack, x, y);
        }
    }
}
