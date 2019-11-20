package org.workcraft.gui.properties;

import java.awt.*;

public class PropertyHelper {

    // Clear symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String CLEAR_SYMBOL = Character.toString((char) 0x00D7);

    // Enter symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String ENTER_SYMBOL = Character.toString((char) 0x21B5);

    // Bullet symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String BULLET_SYMBOL = Character.toString((char) 0x2022);
    public static final String BULLET_PREFIX = "  " + BULLET_SYMBOL + " ";

    public static final Insets BUTTON_INSETS =  new Insets(1, 1, 1, 1);

    public static PropertyDescriptor<String> createSeparatorProperty(String text) {
        return new PropertyDeclaration<>(String.class, "",
                value -> { }, () -> text).setReadonly().setSpan();
    }

}
