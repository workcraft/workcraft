package org.workcraft.dom.visual;

import java.awt.Font;

public class FontHelper {

    public static int getComponentHeightFromFont(Font font) {
        return (int) Math.round(font.getSize2D() * 1.5);
    }

}
