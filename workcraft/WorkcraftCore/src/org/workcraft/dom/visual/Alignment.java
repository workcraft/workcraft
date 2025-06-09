package org.workcraft.dom.visual;

import javax.swing.text.StyleConstants;

public enum Alignment {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    private final String name;

    Alignment(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int toStyleConstant() {
        return switch (this) {
            case LEFT -> StyleConstants.ALIGN_LEFT;
            case CENTER -> StyleConstants.ALIGN_CENTER;
            case RIGHT -> StyleConstants.ALIGN_RIGHT;
        };
    }
}
