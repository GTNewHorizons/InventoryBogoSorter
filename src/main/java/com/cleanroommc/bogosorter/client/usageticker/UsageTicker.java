package com.cleanroommc.bogosorter.client.usageticker;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

// todo: backhand compat
@EventBusSubscriber(side = { Side.CLIENT })
public class UsageTicker {

    public static boolean enableMainHand = true;
    public static boolean enableArmor = true;
    public static boolean enableModule = true;

    private static List<Element> elements = new ArrayList<>();

    public static void reloadElements() {
        elements = new ArrayList<>();

        if (!enableModule) return;

        if (enableMainHand) {
            elements.add(new Element(EquipmentSlotType.MAINHAND));
        }
        if (enableArmor) {
            elements.add(new Element(EquipmentSlotType.HEAD));
            elements.add(new Element(EquipmentSlotType.CHEST));
            elements.add(new Element(EquipmentSlotType.LEGS));
            elements.add(new Element(EquipmentSlotType.FEET));
        }
    }

    @EventBusSubscriber.Condition
    public static boolean enableModule() {
        return enableModule;
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                elements.forEach(e -> e.tick(mc.thePlayer));
            }
        }
    }

    @SubscribeEvent
    public static void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            ScaledResolution res = event.resolution;
            float partialTicks = event.partialTicks;
            elements.forEach(e -> e.render(res, player, partialTicks));
        }
    }

    public static class Element {

        private static final int MAX_ANIM_TICKS = 60;
        private static final int ANIM_TICKS = 5;

        EquipmentSlotType slot;
        ItemStack currentStack = null;
        int currentCount;
        int showTicks;

        public Element(EquipmentSlotType slotType) {
            this.slot = slotType;
        }

        public void render(ScaledResolution resolution, EntityClientPlayerMP player, float partialTicks) {
            if (showTicks > 0) {

                float animProgress;

                if (showTicks < ANIM_TICKS) animProgress = Math.max(0, showTicks - partialTicks) / ANIM_TICKS;
                else animProgress = Math.min(ANIM_TICKS, (MAX_ANIM_TICKS - showTicks) + partialTicks) / ANIM_TICKS;

                float anim = -animProgress * (animProgress - 2) * 19F;

                float x = resolution.getScaledWidth() / 2f;
                float y = resolution.getScaledHeight() - anim;

                int barWidth = 190;
                boolean armor = slot != EquipmentSlotType.MAINHAND;

                int slots = armor ? 4 : 1;
                int index = slots - slot.ordinal() - 1;

                Minecraft mc = Minecraft.getMinecraft();

                x -= (barWidth / 2f) - index * 20;
                x -= slots * 20;

                ItemStack stack = getRenderedStack(player);
                if (stack == null) return;

                RenderItem renderer = new RenderItem();

                GL11.glPushMatrix();
                GL11.glTranslatef(x, y, 0);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                RenderHelper.enableGUIStandardItemLighting();
                renderer.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0);
                renderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0);
                RenderHelper.disableStandardItemLighting();
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
                GL11.glPopMatrix();
            }
        }

        public void tick(EntityClientPlayerMP player) {
            ItemStack stack = getStack(player);

            int count = getStackCount(player, stack);
            stack = getDisplayedStack(stack, count);
            if (stack == null) {
                showTicks = 0;
            } else if (stackChanged(stack, currentStack, count, currentCount)) {
                boolean done = showTicks == 0;
                boolean animatingIn = showTicks > MAX_ANIM_TICKS - ANIM_TICKS;
                boolean animatingOut = showTicks < ANIM_TICKS && !done;
                if (animatingOut) showTicks = MAX_ANIM_TICKS - showTicks;
                else if (!animatingIn) {
                    if (!done) showTicks = MAX_ANIM_TICKS - ANIM_TICKS;
                    else showTicks = MAX_ANIM_TICKS;
                }
            } else if (showTicks > 0) {
                showTicks--;
            }

            currentStack = stack;
            currentCount = count;
        }

        private boolean stackChanged(ItemStack currentStack, ItemStack prevStack, int currentCount, int pastCount) {
            if (currentStack == null && prevStack == null) return false;
            if (currentStack == null) return true;
            if (prevStack == null) return true;

            return !prevStack.isItemEqual(currentStack) || currentCount != pastCount;
        }

        @Nullable
        public ItemStack getStack(EntityClientPlayerMP player) {
            if (slot == EquipmentSlotType.MAINHAND) {
                return player.inventory.getCurrentItem();
            }

            return player.inventory.armorItemInSlot(slot.ordinal() - 1);
        }

        public ItemStack getRenderedStack(EntityClientPlayerMP player) {
            ItemStack curStack = getStack(player);
            if (curStack == null) return null;
            int count = getStackCount(player, curStack);
            ItemStack disStack = getDisplayedStack(curStack, count);
            if (disStack == null) {
                return null;
            }
            // only change the stack size of the copy
            disStack = disStack.copy();
            if (disStack != curStack) {
                count = getStackCount(player, disStack);
            }
            disStack.stackSize = count;
            return disStack;
        }

        public ItemStack getDisplayedStack(ItemStack stack, int count) {
            if (stack == null) return null;
            if (slot == EquipmentSlotType.MAINHAND && !stack.isStackable()) return null;
            if (count == stack.stackSize) return null;

            return stack;
        }

        private int getStackCount(EntityClientPlayerMP player, ItemStack stack) {
            if (stack == null) return 0;

            int total = 0;
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack invStack = player.inventory.getStackInSlot(i);
                if (invStack == null) continue;

                if (invStack.isItemEqual(stack) && invStack.getItemDamage() == stack.getItemDamage()) {
                    total += invStack.stackSize;
                }
            }

            return total;
        }

    }

    public enum EquipmentSlotType {
        MAINHAND,
        HEAD,
        CHEST,
        LEGS,
        FEET
    }

}
