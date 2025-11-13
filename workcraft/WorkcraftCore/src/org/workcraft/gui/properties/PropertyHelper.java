package org.workcraft.gui.properties;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;

import java.awt.*;

public class PropertyHelper {

    // Clear symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String CLEAR_SYMBOL = Character.toString((char) 0x00D7);

    // Enter symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String ENTER_SYMBOL = Character.toString((char) 0x21B5);

    // Bullet symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    public static final String BULLET_SYMBOL = Character.toString((char) 0x2022);

    // Magnifying glass symbol in UTF-8 encoding (avoid inserting UTF symbols directly in the source code).
    // Note: 0x26B2 (looks like a magnifying glass) is often missing, therefore use 0xA4 (looks like aim).
    public static final String SEARCH_SYMBOL = Character.toString((char) 0xA4);

    public static final Insets BUTTON_INSETS = new Insets(1, 1, 1, 1);

    public static PropertyDescriptor<String> createSeparatorProperty(String text) {
        return new PropertyDeclaration<>(String.class, text,
                value -> { }, () -> text).setReadonly().setSpan();
    }

    public static PropertyDescriptor<?> getSignalSectionProperty(VisualModel visualModel) {
        boolean groupByType = SignalCommonSettings.getGroupByType();
        return new ActionDeclaration(
                "Signals " + (groupByType ? "(grouped)" : "(sorted)"),
                groupByType ? "Sort by name" : "Group by type",
                () -> {
                    SignalCommonSettings.setGroupByType(!groupByType);
                    visualModel.sendNotification(new ModelModifiedEvent(visualModel));
                });
    }

    public static String indentWithBullet(String name) {
        return "  " + prependBullet(name);
    }

    public static String prependBullet(String name) {
        return BULLET_SYMBOL + ' ' + (name == null ? "" : name);
    }

}
