package org.workcraft.gui.properties;

import java.awt.*;

public class PropertyUtils {

    // Clear symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final char CLEAR_SYMBOL = 0x00D7;
    public static final String CLEAR_TEXT = Character.toString(CLEAR_SYMBOL);

    // Enter symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final char ENTER_SYMBOL = 0x23CE;
    public static final String ENTER_TEXT = Character.toString(ENTER_SYMBOL);

    // Bullet symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    private static final char BULLET_SYMBOL = 0x2022;
    public static final String BULLET_TEXT = Character.toString(BULLET_SYMBOL);

    public static final Insets BUTTON_INSETS =  new Insets(1, 1, 1, 1);

}
