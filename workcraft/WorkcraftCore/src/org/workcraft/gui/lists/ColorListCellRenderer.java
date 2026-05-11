package org.workcraft.gui.lists;

import org.workcraft.gui.controls.FlatLabel;
import org.workcraft.plugins.builtin.settings.LogCommonSettings;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class ColorListCellRenderer extends DefaultListCellRenderer {

    private final Function<Object, Color> itemToColorConverter;
    private String invalidItemTooltip = null;

    private final JLabel label = new FlatLabel() {
        @Override
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    };

    public ColorListCellRenderer(Function<Object, Color> itemToColorConverter) {
        this.itemToColorConverter = itemToColorConverter;
    }

    @Override
    public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        Color color = itemToColorConverter.apply(value);
        String text = (value == null) ? "" : value.toString();
        if (color == null) {
            if (isSelected) {
                label.setText("<html>&#x26A0&nbsp;" + text + "</html>");
                label.setForeground(list.getForeground());
                label.setBackground(LogCommonSettings.getErrorBackground());
                label.setToolTipText(invalidItemTooltip);
            } else {
                label.setText("<html>&#x26A0&nbsp;<strike>" + text + "</strike></html>");
                label.setForeground(list.getForeground());
                label.setBackground(list.getBackground());
            }
        } else {
            label.setText(text);
            label.setForeground(color);
            label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            boolean fits = GuiUtils.getLabelTextWidth(label) < list.getWidth();
            label.setToolTipText(fits ? null : label.getText());
        }
        return label;
    }

    public void setInvalidItemTooltip(String value) {
        invalidItemTooltip = value;
    }

}
