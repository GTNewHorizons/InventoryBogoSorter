package yalter.mousetweaks.impl;

public enum MouseButton {
    LEFT(0), RIGHT(1);

    private final int id;

    MouseButton(int id) {
        this.id = id;
    }

    public int getValue() {
        return id;
    }
}
