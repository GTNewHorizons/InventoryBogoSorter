package com.cleanroommc.bogosorter.common.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.common.OreDictHelper;
import com.cleanroommc.bogosorter.common.sort.color.ItemColorHelper;
import com.cleanroommc.bogosorter.compat.Mods;

import gregtech.api.interfaces.IFoodStat;
import gregtech.api.items.MetaGeneratedItem;
import moze_intel.projecte.utils.EMCHelper;

public class ItemCompareHelper {

    private static final Pattern SLAB_PATTERN = Pattern.compile(".*Slab([A-Z].*)?");
    private static final Pattern STAIR_PATTERN = Pattern.compile(".*Stairs?([A-Z].*)?");
    private static final Pattern PIPE_PATTERN = Pattern.compile(".*Pipe([A-Z].*)?");
    private static final Pattern CABLE_PATTERN = Pattern.compile(".*Cable([A-Z].*)?");
    private static final Pattern WIRE_PATTERN = Pattern.compile(".*Wire([A-Z].*)?");
    private static final Pattern FORMATTING_PATTERN = Pattern
        .compile("(?i)" + String.valueOf('\u00a7') + "[0-9A-FK-OR]");

    public static String getMod(ItemStack item) {
        String loc = item.getItem().delegate.name();
        if (loc == null) return "";
        return loc.split(":")[0].toLowerCase();
    }

    public static String getId(ItemStack item) {
        String loc = item.getItem().delegate.name();
        if (loc == null) return "";
        return loc.split(":")[1].toLowerCase();
    }

    public static int getMeta(ItemStack item) {
        return item.getItemDamage();
    }

    public static NBTTagCompound getNbt(ItemStack item) {
        return item.getTagCompound();
    }

    public static float getSaturation(ItemStack item) {
        if (item.getItem() instanceof ItemFood) {
            return ((ItemFood) item.getItem()).func_150906_h(item);
        }
        if (Mods.GT5u.isLoaded() && item.getItem() instanceof MetaGeneratedItem) {
            MetaGeneratedItem valueItem = ((MetaGeneratedItem) item.getItem());
            IFoodStat stats = (IFoodStat) valueItem.getFoodValues(item);
            return stats.getSaturation(null, item, null);

        }
        return Float.MIN_VALUE;
    }

    public static int getHunger(ItemStack item) {
        if (item.getItem() instanceof ItemFood) {
            return ((ItemFood) item.getItem()).func_150905_g(item);
        }
        if (Mods.GT5u.isLoaded() && item.getItem() instanceof MetaGeneratedItem) {
            MetaGeneratedItem valueItem = ((MetaGeneratedItem) item.getItem());
            IFoodStat stats = ((IFoodStat) valueItem.getFoodValues(item));
            return stats.getFoodLevel(null, item, null);
        }
        return Integer.MIN_VALUE;
    }

    public static long getEmcValue(ItemStack item) {
        return EMCHelper.getEmcValue(item);
    }

    public static int compareMod(ItemStack stack1, ItemStack stack2) {
        return getMod(stack1).compareTo(getMod(stack2));
    }

    public static int compareId(ItemStack stack1, ItemStack stack2) {
        return getId(stack1).compareTo(getId(stack2));
    }

    @SuppressWarnings("all")
    public static int compareDisplayName(ItemSortContainer stack1, ItemSortContainer stack2) {
        return compareFormattedString(stack1.getName(), stack2.getName());
    }

    public static int compareDisplayName(ItemStack stack1, ItemStack stack2) {
        return compareFormattedString(stack1.getDisplayName(), stack2.getDisplayName());
    }

    @SuppressWarnings("all")
    public static int compareFormattedString(String s1, String s2) {
        return getTextWithoutFormattingCodes(s1).compareTo(getTextWithoutFormattingCodes(s2));
    }

    // used for display name formatting on client AND server; 1.7 is client only, 1.12 is on both
    private static String getTextWithoutFormattingCodes(String str) {
        return str == null ? null
            : FORMATTING_PATTERN.matcher(str)
                .replaceAll("");
    }

    public static int compareMeta(ItemStack stack1, ItemStack stack2) {
        return Integer.compare(getMeta(stack1), getMeta(stack2));
    }

    public static int compareCount(ItemStack stack1, ItemStack stack2) {
        return Integer.compare(stack2.stackSize, stack1.stackSize);
    }

    public static int compareRegistryOrder(ItemStack stack1, ItemStack stack2) {
        Item item1 = stack1.getItem();
        Item item2 = stack2.getItem();

        int id1 = Item.itemRegistry.getIDForObject(item1);
        int id2 = Item.itemRegistry.getIDForObject(item2);

        return Integer.compare(id1, id2);
    }

    public static int compareOreDict(ItemStack stack1, ItemStack stack2) {
        List<String> ores1 = new ArrayList<>(OreDictHelper.getOreDicts(stack1));
        List<String> ores2 = new ArrayList<>(OreDictHelper.getOreDicts(stack2));
        if (ores1.isEmpty() && ores2.isEmpty()) return 0;
        if (ores1.size() != ores2.size()) {
            return Integer.compare(ores1.size(), ores2.size());
        }
        if (ores1.size() > 1) {
            ores1.sort(String::compareTo);
            ores2.sort(String::compareTo);
        }
        int val = 0;
        for (int i = 0, n = ores1.size(); i < n; i++) {
            val += ores1.get(i)
                .compareTo(ores2.get(i));
        }
        return MathHelper.clamp_int(val, -1, 1);
    }

    public static int compareHasNbt(ItemStack stack1, ItemStack stack2) {
        NBTTagCompound nbt1 = stack1.getTagCompound();
        NBTTagCompound nbt2 = stack2.getTagCompound();
        if ((nbt1 == null) == (nbt2 == null)) return 0;
        if (nbt1 == null) return -1;
        return 1;
    }

    public static int compareNotNullNbt(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int result = compareNbtSize(nbt1, nbt2);
        if (result != 0) return result;
        return compareNbtValues(nbt1, nbt2);
    }

    public static int compareNbtValues(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound() ? compareNbtValues(itemStack1.getTagCompound(), itemStack2.getTagCompound())
            : 0;
    }

    public static int compareNbtValues(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int result = 0;
        for (NbtSortRule nbtSortRule : SortHandler.currentNbtSortRules.get()) {
            result = nbtSortRule.compare(nbt1, nbt2);
            if (result != 0) return result;
        }
        return result;
    }

    public static int compareNbtAllValues(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound()
            ? compareNbtAllValues(itemStack1.getTagCompound(), itemStack2.getTagCompound())
            : 0;
    }

    public static int compareNbtAllValues(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        int total = 0;
        for (String key : nbt1.func_150296_c()) {
            if (nbt2.hasKey(key)) {
                int result = compareNbtBase(nbt1.getTag(key), nbt2.getTag(key));
                total += result;
            }
        }
        return MathHelper.clamp_int(total, -1, 1);
    }

    public static int compareNbtBase(NBTBase nbt1, NBTBase nbt2) {
        if (nbt1.getId() != nbt2.getId()) return 0;
        if (nbt1.getId() == Constants.NBT.TAG_COMPOUND) {
            return compareNbtAllValues((NBTTagCompound) nbt1, (NBTTagCompound) nbt2);
        }
        if (nbt1 instanceof NBTBase.NBTPrimitive) {
            return Double
                .compare(((NBTBase.NBTPrimitive) nbt1).func_150286_g(), ((NBTBase.NBTPrimitive) nbt2).func_150286_g());
        }
        if (nbt1.getId() == Constants.NBT.TAG_BYTE_ARRAY) {
            byte[] array1 = ((NBTTagByteArray) nbt1).func_150292_c();
            byte[] array2 = ((NBTTagByteArray) nbt2).func_150292_c();
            if (array1.length != array2.length) {
                return array1.length < array2.length ? -1 : 1;
            }
            int total = 0;
            for (int i = 0; i < array1.length; i++) {
                total += Byte.compare(array1[i], array2[i]);
            }
            return total;
        }
        if (nbt1.getId() == Constants.NBT.TAG_INT_ARRAY) {
            int[] array1 = ((NBTTagIntArray) nbt1).func_150302_c();
            int[] array2 = ((NBTTagIntArray) nbt2).func_150302_c();
            if (array1.length != array2.length) {
                return array1.length < array2.length ? -1 : 1;
            }
            int total = 0;
            for (int i = 0; i < array1.length; i++) {
                total += Integer.compare(array1[i], array2[i]);
            }
            return total;
        }
        // if (nbt1.getId() == Constants.NBT.TAG_LONG_ARRAY) {
        // // TODO for some fucking reason long array tag doesn't have a array getter
        // return 0;
        // }
        return 0;
    }

    public static int compareNbtSize(ItemStack itemStack1, ItemStack itemStack2) {
        int result = compareHasNbt(itemStack1, itemStack2);
        if (result != 0) return result;
        return itemStack1.hasTagCompound() ? compareNbtSize(itemStack1.getTagCompound(), itemStack2.getTagCompound())
            : 0;
    }

    public static int compareNbtSize(@NotNull NBTTagCompound nbt1, @NotNull NBTTagCompound nbt2) {
        if (nbt1.tagMap.size() < nbt2.tagMap.size()) return -1;
        if (nbt1.tagMap.size() > nbt2.tagMap.size()) return 1;
        List<NBTTagCompound> subTags1 = new ArrayList<>();
        List<NBTTagCompound> subTags2 = new ArrayList<>();
        subTags1.add(nbt1);
        subTags2.add(nbt2);
        while (true) {
            subTags1 = getAllSubTags(subTags1);
            subTags2 = getAllSubTags(subTags2);
            int size1 = getTotalSubTags(subTags1);
            int size2 = getTotalSubTags(subTags2);
            if (size1 == 0 && size2 == 0) return 0;
            if (size1 < size2) return -1;
            if (size1 > size2) return 1;
        }
    }

    @Nullable
    public static NBTBase findSubTag(String path, NBTBase tag) {
        if (tag == null || path == null || path.isEmpty()) return null;
        String[] parts = path.split("/");
        for (String part : parts) {
            if (tag == null || tag.getId() != 10) return null;
            tag = ((NBTTagCompound) tag).getTag(part);
        }
        return tag;
    }

    private static List<NBTTagCompound> getAllSubTags(List<NBTTagCompound> tags) {
        List<NBTTagCompound> subTags = new ArrayList<>();
        for (NBTTagCompound nbt : tags) {
            for (String key : nbt.func_150296_c()) {
                NBTBase nbtBase = nbt.getTag(key);
                if (nbt.getTag(key) instanceof NBTTagCompound) {
                    subTags.add((NBTTagCompound) nbtBase);
                }
            }
        }
        return subTags;
    }

    private static int getTotalSubTags(List<NBTTagCompound> tags) {
        int sum = 0;
        for (NBTTagCompound nbt : tags) {
            sum += nbt.tagMap.size();
        }
        return sum;
    }

    public static int comparePotionId(String potion1, String potion2) {
        String id1 = potion1.startsWith("strong") || potion1.startsWith("long")
            ? potion1.substring(potion1.indexOf('_') + 1)
            : potion1;
        String id2 = potion2.startsWith("strong") || potion2.startsWith("long")
            ? potion2.substring(potion2.indexOf('_') + 1)
            : potion2;
        int result = id1.compareTo(id2);
        if (result != 0) return result;
        boolean strong1 = potion1.startsWith("strong");
        boolean strong2 = potion2.startsWith("strong");
        if (strong1 && !strong2) return 1;
        if (!strong1 && strong2) return -1;
        return Boolean.compare(potion1.startsWith("long"), potion2.startsWith("long"));
    }

    public static int compareEnchantments(NBTTagList enchantments1, NBTTagList enchantments2) {
        int total1 = 0;
        for (int i = 0; i < enchantments1.tagCount(); i++) {
            NBTTagCompound nbt = enchantments1.getCompoundTagAt(i);
            total1 += nbt.getShort("id");
        }
        int total2 = 0;
        for (int i = 0; i < enchantments2.tagCount(); i++) {
            NBTTagCompound nbt = enchantments2.getCompoundTagAt(i);
            total2 += nbt.getShort("id");
        }
        int result = Integer.compare(total1, total2);
        if (result != 0) return result;
        total1 = 0;
        for (int i = 0; i < enchantments1.tagCount(); i++) {
            NBTTagCompound nbt = enchantments1.getCompoundTagAt(i);
            total1 += nbt.getShort("lvl");
        }
        total2 = 0;
        for (int i = 0; i < enchantments2.tagCount(); i++) {
            NBTTagCompound nbt = enchantments2.getCompoundTagAt(i);
            total2 += nbt.getShort("lvl");
        }
        return Integer.compare(total1, total2);
    }

    public static int compareEnchantment(NBTTagCompound enchantment1, NBTTagCompound enchantment2) {
        int result = Integer.compare(enchantment1.getShort("id"), enchantment2.getShort("id"));
        if (result != 0) return result;
        return Integer.compare(enchantment1.getShort("lvl"), enchantment2.getShort("lvl"));
    }

    public static int compareMaterial(ItemStack item1, ItemStack item2) {
        String mat1 = OreDictHelper.getMaterial(item1);
        String mat2 = OreDictHelper.getMaterial(item2);
        if (mat1 == null && mat2 == null) return 0;
        if (mat1 == null) return 1;
        return mat2 == null ? -1 : mat1.compareTo(mat2);
    }

    public static int compareOrePrefix(ItemStack item1, ItemStack item2) {
        String prefix = OreDictHelper.getOrePrefix(item1);
        String prefix1 = OreDictHelper.getOrePrefix(item2);
        if (prefix == null && prefix1 == null) return 0;
        if (prefix == null) return 1;
        if (prefix1 == null) return -1;
        return Integer.compare(OreDictHelper.getOrePrefixIndex(prefix), OreDictHelper.getOrePrefixIndex(prefix1));
    }

    public static int compareEMC(ItemStack item1, ItemStack item2) {
        return Long.compare(getEmcValue(item2), getEmcValue(item1));
    }

    public static int compareBlockType(ItemStack item1, ItemStack item2) {
        int c = Boolean.compare(isBlock(item2), isBlock(item1));
        if (c != 0 || !isBlock(item1)) return c;
        Block block1 = getBlock(item1);
        Block block2 = getBlock(item2);
        Block state1 = getBlockState(block1, item1.getItemDamage());
        Block state2 = getBlockState(block2, item2.getItemDamage());
        c = Boolean.compare(state2.func_149730_j(), state1.func_149730_j());
        if (c != 0) return c;
        c = Boolean.compare(state2.renderAsNormalBlock(), state1.renderAsNormalBlock());
        if (c != 0) return c;
        c = Boolean.compare(state2.isOpaqueCube(), state1.isOpaqueCube());
        if (c != 0) return c;
        c = Boolean.compare(isSlab(block2), isSlab(block1));
        if (c != 0) return c;
        c = Boolean.compare(isStair(block2), isStair(block1));
        if (c != 0) return c;
        c = Boolean.compare(isPipe(block2), isPipe(block1));
        if (c != 0) return c;
        return Boolean.compare(block1.hasTileEntity(0), block2.hasTileEntity(0));
    }

    public static int compareBurnTime(ItemStack item1, ItemStack item2) {
        return Integer.compare(TileEntityFurnace.getItemBurnTime(item2), TileEntityFurnace.getItemBurnTime(item1));
    }

    public static int compareSaturation(ItemStack item1, ItemStack item2) {
        return Float.compare(getSaturation(item2), getSaturation(item1));
    }

    public static int compareHunger(ItemStack item1, ItemStack item2) {
        return Integer.compare(getHunger(item2), getHunger(item1));
    }

    public static int compareColor(ItemSortContainer item1, ItemSortContainer item2) {
        return Integer.compare(item1.getColorHue(), item2.getColorHue());
    }

    public static int compareColor(ItemStack item1, ItemStack item2) {
        return Integer.compare(ItemColorHelper.getItemColorHue(item1), ItemColorHelper.getItemColorHue(item2));
    }

    public static Block getBlockState(Block block, int meta) {
        try {
            return block.getBlockById(meta);
        } catch (Exception e) {
            return block;
        }
    }

    public static boolean isBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemReed;
    }

    public static Block getBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock ? ((ItemBlock) stack.getItem()).field_150939_a
            : ((ItemReed) stack.getItem()).field_150935_a;
    }

    public static boolean isSlab(Block block) {
        return block instanceof BlockSlab || SLAB_PATTERN.matcher(
            block.getClass()
                .getSimpleName())
            .matches();
    }

    public static boolean isStair(Block block) {
        return block instanceof BlockStairs || STAIR_PATTERN.matcher(
            block.getClass()
                .getSimpleName())
            .matches();
    }

    public static boolean isPipe(Block block) {
        return PIPE_PATTERN.matcher(
            block.getClass()
                .getSimpleName())
            .matches()
            || CABLE_PATTERN.matcher(
                block.getClass()
                    .getSimpleName())
                .matches()
            || WIRE_PATTERN.matcher(
                block.getClass()
                    .getSimpleName())
                .matches();
    }
}
