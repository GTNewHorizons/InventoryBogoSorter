package com.cleanroommc.bogosorter.common.sort;

import java.util.Objects;

import com.cleanroommc.bogosorter.api.IButtonPos;

public class ButtonPos implements IButtonPos {

    private boolean enabled = true;
    private int x = 0, y = 0;
    private Alignment alignment = Alignment.BOTTOM_RIGHT;
    private Layout layout = Layout.HORIZONTAL;

    public void reset() {
        this.enabled = true;
        this.x = 0;
        this.y = 0;
        this.alignment = Alignment.BOTTOM_RIGHT;
        this.layout = Layout.HORIZONTAL;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setAlignment(Alignment alignment) {
        this.alignment = Objects.requireNonNull(alignment);
    }

    @Override
    public void setLayout(Layout layout) {
        this.layout = Objects.requireNonNull(layout);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public Alignment getAlignment() {
        return this.alignment;
    }

    @Override
    public Layout getLayout() {
        return this.layout;
    }

    public void applyPos(int guiLeft, int guiTop, ButtonHandler.SortButton sortButton,
        ButtonHandler.SortButton settingsButton) {
        int s = ButtonHandler.BUTTON_SIZE;
        boolean h = this.layout == Layout.HORIZONTAL;
        switch (this.alignment) {
            case TOP_LEFT: {
                sortButton.xPosition = this.x;
                sortButton.yPosition = this.y;
                break;
            }
            case TOP_RIGHT: {
                sortButton.xPosition = h ? this.x - s - s : this.x - s;
                sortButton.yPosition = this.y;
                break;
            }
            case BOTTOM_LEFT: {
                sortButton.xPosition = this.x;
                sortButton.yPosition = h ? this.y - s : this.y - s - s;
                break;
            }
            case BOTTOM_RIGHT: {
                sortButton.xPosition = h ? this.x - s - s : this.x - s;
                sortButton.yPosition = h ? this.y - s : this.y - s - s;
                break;
            }
        }
        sortButton.xPosition += guiLeft;
        sortButton.yPosition += guiTop;
        if (h) {
            settingsButton.xPosition = sortButton.xPosition + s;
            settingsButton.yPosition = sortButton.yPosition;
        } else {
            settingsButton.xPosition = sortButton.xPosition;
            settingsButton.yPosition = sortButton.yPosition + s;
        }
    }
}
