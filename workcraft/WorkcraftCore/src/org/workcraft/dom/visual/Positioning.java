package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

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
        this.xSign = Double.compare(xOffset, 0.0);
        this.ySign = Double.compare(yOffset, 0.0);
    }

    @Override
    public String toString() {
        return name;
    }

    public AffineTransform getTransform() {
        AffineTransform result = new AffineTransform();
        switch (this) {
            case TOP -> result.rotate(1.0, 0.0);
            case RIGHT -> result.rotate(0.0, 1.0);
            case BOTTOM -> result.rotate(-1.0, 0.0);
            case LEFT -> result.rotate(0.0, -1.0);
            case BOTTOM_LEFT -> result.rotate(-1.0, -1.0);
            case BOTTOM_RIGHT -> result.rotate(-1.0, 1.0);
            case CENTER -> result.rotate(0.0, 0.0);
            case TOP_LEFT -> result.rotate(1.0, -1.0);
            case TOP_RIGHT -> result.rotate(1.0, 1.0);
        }
        return result;
    }

    public Positioning flipHorizontal() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case TOP_LEFT -> TOP_RIGHT;
            case TOP_RIGHT -> TOP_LEFT;
            case BOTTOM_LEFT -> BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> BOTTOM_LEFT;
            default -> this;
        };
    }

    public Positioning flipVertical() {
        return switch (this) {
            case TOP -> BOTTOM;
            case BOTTOM -> TOP;
            case TOP_LEFT -> BOTTOM_LEFT;
            case TOP_RIGHT -> BOTTOM_RIGHT;
            case BOTTOM_LEFT -> TOP_LEFT;
            case BOTTOM_RIGHT -> TOP_RIGHT;
            default -> this;
        };
    }

    public Positioning rotateClockwise() {
        return switch (this) {
            case TOP -> RIGHT;
            case BOTTOM -> LEFT;
            case LEFT -> TOP;
            case RIGHT -> BOTTOM;
            case TOP_LEFT -> TOP_RIGHT;
            case TOP_RIGHT -> BOTTOM_RIGHT;
            case BOTTOM_LEFT -> TOP_LEFT;
            case BOTTOM_RIGHT -> BOTTOM_LEFT;
            default -> this;
        };
    }

    public Positioning rotateCounterclockwise() {
        return switch (this) {
            case TOP -> LEFT;
            case BOTTOM -> RIGHT;
            case LEFT -> BOTTOM;
            case RIGHT -> TOP;
            case TOP_LEFT -> BOTTOM_LEFT;
            case TOP_RIGHT -> TOP_LEFT;
            case BOTTOM_LEFT -> BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> TOP_RIGHT;
            default -> this;
        };
    }

}
