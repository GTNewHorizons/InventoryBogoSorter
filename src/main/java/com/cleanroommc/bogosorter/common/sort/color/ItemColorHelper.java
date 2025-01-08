package com.cleanroommc.bogosorter.common.sort.color;

import java.awt.image.BufferedImage;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.BogoSortAPI;
import com.cleanroommc.modularui.utils.Color;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class ItemColorHelper {

    private static final Object2IntMap<ItemStack> ITEM_COLORS = new Object2IntOpenCustomHashMap<>(
        BogoSortAPI.ITEM_META_NBT_HASH_STRATEGY);

    public static int getHue(int color) {
        if (color == 0) return 0;
        float r = Color.getRedF(color);
        float g = Color.getGreenF(color);
        float b = Color.getBlueF(color);
        if (r == g && r == b) return 0;
        float min = Math.min(r, Math.min(g, b));
        float hue;
        if (r >= g && r >= b) {
            hue = ((g - b) / (r - min)) % 6;
        } else if (g >= r && g >= b) {
            hue = ((b - r) / (g - min)) + 2;
        } else {
            hue = ((r - g) / (b - min)) + 4;
        }
        hue *= 60;
        if (hue < 0) hue += 360;
        return (int) hue;
    }

    public static int getItemColorHue(ItemStack item) {
        if (item == null) {
            return 362;
        }
        if (ITEM_COLORS.containsKey(item)) {
            return ITEM_COLORS.getInt(item);
        }
        int hue;
        try {
            hue = getHue(getAverageItemColor(item));
        } catch (Exception e) {
            hue = 361;
        }
        ITEM_COLORS.put(item, hue);
        return hue;
    }

    public static int getAverageItemColor(ItemStack item) {
        if (item.getItem() instanceof ItemBlock) {
            return getBlockColors(item, ((ItemBlock) item.getItem()).field_150939_a);
        } else {
            return getItemColors(item);
        }
    }

    private static int getItemColors(ItemStack itemStack) {
        try {
            final ItemColored itemColors = new ItemColored(
                ((ItemBlock) Objects.requireNonNull(itemStack.getItem())).field_150939_a,
                false);
            final int renderColor = itemColors.getColorFromItemStack(itemStack, 0);
            final TextureAtlasSprite textureAtlasSprite = getTextureAtlasSprite(itemStack);
            return getColors(textureAtlasSprite, renderColor);
        } catch (Exception ignored) {
            // this will crash for most if not all eu2 items and there is nothing I can do about it except this
            return 0;
        }
    }

    private static int getBlockColors(ItemStack itemStack, Block block) {
        final int meta = itemStack.getItemDamage();
        Block State;
        try {
            State = block.getBlockById(meta);
        } catch (RuntimeException | LinkageError ignored) {
            State = block;
        }

        int renderColor = 0xFFFFFFFF;
        try {
            State.colorMultiplier(null, 0, 0, 0);
        } catch (Exception ignored) {}
        final TextureAtlasSprite textureAtlasSprite;
        if (true) { // && blockState.getBlock() instanceof BlockMachine) {
            // MetaTileEntity mte = GTUtility.getMetaTileEntity(itemStack);
            // Pair<TextureAtlasSprite, Integer> pair = mte.getParticleTexture();
            // textureAtlasSprite = pair.getKey();
            // renderColor = pair.getRight();
            return 0;
        } else {
            textureAtlasSprite = getTextureAtlasSprite(block);
        }
        return getColors(textureAtlasSprite, renderColor);
    }

    public static int getColors(TextureAtlasSprite textureAtlasSprite, int renderColor) {
        int[] color;
        if (textureAtlasSprite == null) {
            color = new int[] { 0, 0, 0 };
        } else {
            final BufferedImage bufferedImage = getBufferedImage(textureAtlasSprite);
            if (bufferedImage == null) {
                color = new int[] { 0, 0, 0 };
            } else {
                color = ColorThief.getColor(bufferedImage, 10, true);
            }
        }
        if (color == null) return 0;
        int red = (int) ((color[0] - 1) * (float) (renderColor >> 16 & 255) / 255.0F);
        int green = (int) ((color[1] - 1) * (float) (renderColor >> 8 & 255) / 255.0F);
        int blue = (int) ((color[2] - 1) * (float) (renderColor & 255) / 255.0F);
        red = MathHelper.clamp_int(red, 0, 255);
        green = MathHelper.clamp_int(green, 0, 255);
        blue = MathHelper.clamp_int(blue, 0, 255);
        return Color.rgb(red, green, blue);
    }

    @Nullable
    private static BufferedImage getBufferedImage(TextureAtlasSprite textureAtlasSprite) {
        final int iconWidth = textureAtlasSprite.getIconWidth();
        final int iconHeight = textureAtlasSprite.getIconHeight();
        final int frameCount = textureAtlasSprite.getFrameCount();
        if (iconWidth <= 0 || iconHeight <= 0 || frameCount <= 0) {
            return null;
        }

        BufferedImage bufferedImage = new BufferedImage(
            iconWidth,
            iconHeight * frameCount,
            BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frameCount; i++) {
            int[][] frameTextureData = textureAtlasSprite.getFrameTextureData(i);
            int[] largestMipMapTextureData = frameTextureData[0];
            bufferedImage.setRGB(0, i * iconHeight, iconWidth, iconHeight, largestMipMapTextureData, 0, iconWidth);
        }

        return bufferedImage;
    }

    @Nullable
    private static TextureAtlasSprite getTextureAtlasSprite(Block block) {
        Minecraft minecraft = Minecraft.getMinecraft();
        TextureMap textureMap = minecraft.getTextureMapBlocks();
        IIcon icon = block.getBlockTextureFromSide(0);
        TextureAtlasSprite textureAtlasSprite = textureMap.getTextureExtry(icon.getIconName());
        if (textureAtlasSprite == textureMap.getAtlasSprite("missingno")) {
            return null;
        }
        return textureAtlasSprite;
    }

    private static TextureAtlasSprite getTextureAtlasSprite(ItemStack itemStack) {
        // RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        // ItemModelMesher itemModelMesher = renderItem.getItemModelMesher();
        // IBakedModel itemModel = itemModelMesher.getItemModel(itemStack);
        return null; // itemModel.getParticleTexture();
    }
}
