package com.cleanroommc.bogosorter.client.keybinds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.cleanroommc.bogosorter.ClientEventHandler;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * A key combo containing key which must be pressed and which must not be pressed in order for the key to activate.
 * Works only in GUI.
 */
public class KeyBind {

    private static final List<KeyBind> keyBinds = new ArrayList<>();

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final String name;
    private final int[] keys;
    private final int[] notKeys;
    private final BooleanSupplier additionalValidator;

    private long pressedTick = -1;

    public KeyBind(String name, int[] keys, int[] notKeys, BooleanSupplier additionalValidator) {
        this.name = name;
        this.keys = keys;
        this.notKeys = notKeys;
        this.additionalValidator = additionalValidator;
        keyBinds.add(this);
    }

    private static boolean isKey(int key) {
        return key != Integer.MIN_VALUE;
    }

    public static boolean isKeyPressed(int key) {
        if (key < 0) return Mouse.isButtonDown(key + 100);
        return Keyboard.isKeyDown(key);
    }

    public static boolean areAllPressed(int[] keys) {
        if (keys.length == 0) return true;
        if (keys.length == 1) return isKeyPressed(keys[0]);
        for (int key : keys) {
            if (!isKeyPressed(key)) return false;
        }
        return true;
    }

    public static boolean areAllNotPressed(int[] keys) {
        if (keys.length == 0) return true;
        if (keys.length == 1) return !isKeyPressed(keys[0]);
        for (int key : keys) {
            if (isKeyPressed(key)) return false;
        }
        return true;
    }

    public static void checkKeys(long currentTick) {
        for (KeyBind key : keyBinds) {
            key.checkPressed(currentTick);
        }
    }

    protected void checkPressed(long currentTick) {
        if (areAllPressed(this.keys) && areAllNotPressed(this.notKeys) && this.additionalValidator.getAsBoolean()) {
            if (this.pressedTick < 0) {
                this.pressedTick = currentTick;
            }
        } else {
            this.pressedTick = -1;
        }
    }

    public boolean isPressed() {
        return this.pressedTick >= 0;
    }

    public int getTicksPressed() {
        return this.pressedTick < 0 ? -1 : (int) (ClientEventHandler.getTicks() - this.pressedTick);
    }

    public boolean isFirstPress() {
        return getTicksPressed() == 0;
    }

    public boolean isFirstPressOrHeldLong(int amount) {
        int ticks = getTicksPressed();
        return ticks == 0 || ticks >= amount;
    }

    public String getName() {
        return name;
    }

    public static class Builder {

        private final String name;
        private final IntArrayList keys = new IntArrayList();
        private final IntArrayList notKeys = new IntArrayList();
        private BooleanSupplier validator = null;

        public Builder(String name) {
            this.name = name;
        }

        private Builder add(boolean press, int key) {
            if (press) {
                keys.add(key);
            } else {
                notKeys.add(key);
            }
            return this;
        }

        public Builder mustPress(int... keys) {
            for (int key : keys) this.keys.add(key);
            return this;
        }

        public Builder mustNotPress(int... keys) {
            for (int key : keys) this.notKeys.add(key);
            return this;
        }

        public Builder lmb(boolean press) {
            return add(press, -100);
        }

        public Builder rmb(boolean press) {
            return add(press, -99);
        }

        public Builder ctrl(boolean press) {
            if (Minecraft.isRunningOnMac) {
                return add(press, 219);
            }
            return add(press, 29);
        }

        public Builder shift(boolean press) {
            return add(press, 42);
        }

        public Builder alt(boolean press) {
            return add(press, 56);
        }

        public Builder space(boolean press) {
            return add(press, Keyboard.KEY_SPACE);
        }

        public Builder validator(BooleanSupplier validator) {
            this.validator = validator;
            return this;
        }

        public KeyBind build() {
            keys.trim();
            notKeys.trim();
            return new KeyBind(
                this.name,
                keys.elements(),
                notKeys.elements(),
                validator != null ? validator : () -> true);
        }
    }
}
