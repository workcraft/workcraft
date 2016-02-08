package org.workcraft.dom.visual;

import javax.swing.text.StyleConstants;

public enum Alignment {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right");

    private final String name;

    private Alignment(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public int toStyleConstant() {
        int result = StyleConstants.ALIGN_CENTER;
        switch (this) {
        case LEFT:
            result = StyleConstants.ALIGN_LEFT;
            break;
        case CENTER:
            result = StyleConstants.ALIGN_CENTER;
            break;
        case RIGHT:
            result = StyleConstants.ALIGN_RIGHT;
            break;
        }
        return result;
    }
}
