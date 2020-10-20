package org.workcraft.gui.properties;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

class ColorComboBoxRenderer extends DefaultListCellRenderer {

    @Override
    public void paint(Graphics g) {
        GuiUtils.paintBackgroundColor(g, new Rectangle(getSize()), getBackground());
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, " ", index, isSelected, cellHasFocus);

        if (value instanceof Color) {
            setBackground((Color) value);
        }
        return this;
    }

}
