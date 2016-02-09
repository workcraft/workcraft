package org.workcraft.dom.visual;

import java.awt.Font;
import java.awt.Toolkit;

public class FontHelper {

    public static int getFontSizeInPixels(Font font) {
        int screenDpi = Toolkit.getDefaultToolkit().getScreenResolution();
        return (int)Math.round(font.getSize2D() * screenDpi / 72.0);
    }

}
