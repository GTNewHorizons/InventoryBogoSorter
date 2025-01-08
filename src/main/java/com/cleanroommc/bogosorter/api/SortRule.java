package com.cleanroommc.bogosorter.api;

import java.util.Comparator;

public class SortRule<T> implements Comparator<T> {

    private static int nextId = 0;

    private final String key;
    private final Comparator<T> comparator;
    private boolean inverted = false;
    private final int syncId;

    public SortRule(String key, Comparator<T> comparator) {
        this.key = key;
        this.comparator = comparator;
        this.syncId = nextId++;
    }

    public String getKey() {
        return key;
    }

    public String getNameLangKey() {
        return "bogosort.sortrules.item." + key + ".name";
    }

    public String getDescriptionLangKey() {
        return "bogosort.sortrules.item." + key + ".description";
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public int getSyncId() {
        return syncId;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public int compare(T o1, T o2) {
        return inverted ? comparator.compare(o2, o1) : comparator.compare(o1, o2);
    }

    @Override
    public String toString() {
        return "SortRule{" + "key='" + key + '\'' + '}';
    }
}
