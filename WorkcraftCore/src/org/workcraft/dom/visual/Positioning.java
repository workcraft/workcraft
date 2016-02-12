package org.workcraft.dom.visual;

public enum Positioning {
    TOP("Top", 0.0, -0.2),
    BOTTOM("Bottom", 0.0, 0.2),
    LEFT("Left", -0.2, 0.0),
    RIGHT("Right", 0.2, 0.0),
    CENTER("Center", 0.0, 0.0),
    TOP_LEFT("Top-Left", -0.2, -0.2),
    TOP_RIGHT("Top-Right", 0.2, -0.2),
    BOTTOM_LEFT("Bottom-Left", -0.2, 0.2),
    BOTTOM_RIGHT("Bottom-Right", 0.2, 0.2);

    public final String name;
    public final double xOffset;
    public final double yOffset;
    public final int xSign;
    public final int ySign;

    Positioning(String name, double xOffset, double yOffset) {
        this.name = name;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xSign = (xOffset == 0.0) ? 0 : (xOffset > 0.0) ? 1 : -1;
        this.ySign = (yOffset == 0.0) ? 0 : (yOffset > 0.0) ? 1 : -1;
    }

    @Override
    public String toString() {
        return name;
    }

    public Positioning flipHorizontal() {
        switch (this) {
        case LEFT: return RIGHT;
        case RIGHT: return LEFT;
        case TOP_LEFT:    return TOP_RIGHT;
        case TOP_RIGHT:    return TOP_LEFT;
        case BOTTOM_LEFT:    return BOTTOM_RIGHT;
        case BOTTOM_RIGHT:    return BOTTOM_LEFT;
        default: return this;
        }
    }

    public Positioning flipVertical() {
        switch (this) {
        case TOP: return BOTTOM;
        case BOTTOM: return TOP;
        case TOP_LEFT:    return BOTTOM_LEFT;
        case TOP_RIGHT:    return BOTTOM_RIGHT;
        case BOTTOM_LEFT:    return TOP_LEFT;
        case BOTTOM_RIGHT:    return TOP_RIGHT;
        default: return this;
        }
    }

    public Positioning rotateClockwise() {
        switch (this) {
        case TOP: return RIGHT;
        case BOTTOM: return LEFT;
        case LEFT: return TOP;
        case RIGHT: return BOTTOM;
        case TOP_LEFT:    return TOP_RIGHT;
        case TOP_RIGHT:    return BOTTOM_RIGHT;
        case BOTTOM_LEFT:    return TOP_LEFT;
        case BOTTOM_RIGHT:    return BOTTOM_LEFT;
        default: return this;
        }
    }

    public Positioning rotateCounterclockwise() {
        switch (this) {
        case TOP: return LEFT;
        case BOTTOM: return RIGHT;
        case LEFT: return BOTTOM;
        case RIGHT: return TOP;
        case TOP_LEFT:    return BOTTOM_LEFT;
        case TOP_RIGHT:    return TOP_LEFT;
        case BOTTOM_LEFT:    return BOTTOM_RIGHT;
        case BOTTOM_RIGHT:    return TOP_RIGHT;
        default: return this;
        }
    }

}
