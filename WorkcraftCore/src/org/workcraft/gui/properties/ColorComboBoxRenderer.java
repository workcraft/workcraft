package org.workcraft.gui.properties;

import javax.swing.*;
import java.awt.*;

class ColorComboBoxRenderer implements ListCellRenderer {

    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        Component renderer = defaultRenderer.getListCellRendererComponent(
                list, " ", index, isSelected, cellHasFocus);

        if (value instanceof Color) {
            renderer.setBackground((Color) value);
        }
        return renderer;
    }

}
