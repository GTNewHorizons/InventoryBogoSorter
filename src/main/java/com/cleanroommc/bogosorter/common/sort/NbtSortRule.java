package com.cleanroommc.bogosorter.common.sort;

import java.util.Comparator;
import java.util.function.Function;

import net.minecraft.nbt.NBTBase;

import com.cleanroommc.bogosorter.api.SortRule;

public class NbtSortRule extends SortRule<NBTBase> {

    private final String tagPath;

    public NbtSortRule(String key, String tagPath, Comparator<NBTBase> comparator) {
        super(key, createComparator(tagPath, comparator));
        this.tagPath = tagPath;
    }

    public NbtSortRule(String key, String tagPath, int expectedType) {
        this(key, tagPath, createComparator(expectedType, ItemCompareHelper::compareNbtBase, nbtBase -> nbtBase));
    }

    public <T> NbtSortRule(String key, String tagPath, int expectedType, Comparator<T> comparator,
        Function<NBTBase, T> converter) {
        this(key, tagPath, createComparator(expectedType, comparator, converter));
    }

    private static Comparator<NBTBase> createComparator(String path, Comparator<NBTBase> comparator) {
        return (o1, o2) -> {
            if (o1 == null || o2 == null) return 0;
            NBTBase nbt1 = ItemCompareHelper.findSubTag(path, o1);
            NBTBase nbt2 = ItemCompareHelper.findSubTag(path, o2);
            return comparator.compare(nbt1, nbt2);
        };
    }

    private static <T> Comparator<NBTBase> createComparator(int expectedType, Comparator<T> comparator,
        Function<NBTBase, T> converter) {
        return (o1, o2) -> {
            if (o1 == o2) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            if (o1.getId() != expectedType && o2.getId() != expectedType) return 0;
            if (o1.getId() != expectedType) return -1;
            if (o2.getId() != expectedType) return 1;
            T t1 = converter.apply(o1);
            T t2 = converter.apply(o2);
            if (t1 == t2) return 0;
            if (t1 == null) return -1;
            if (t2 == null) return 1;
            return comparator.compare(t1, t2);
        };
    }

    @Override
    public String getNameLangKey() {
        return "bogosort.sortrules.nbt." + getKey() + ".name";
    }

    @Override
    public String getDescriptionLangKey() {
        return "bogosort.sortrules.nbt." + getKey() + ".description";
    }
}
