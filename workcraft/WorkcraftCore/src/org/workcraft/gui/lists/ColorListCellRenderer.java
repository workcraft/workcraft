package org.workcraft.gui.lists;

import org.workcraft.gui.controls.FlatLabel;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class ColorListCellRenderer extends DefaultListCellRenderer {

    private final Function<Object, Color> itemToColorConverter;

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
        label.setForeground(color == null ? list.getForeground() : color);
        label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        label.setText(value == null ? "" : value.toString());

        boolean fits = GuiUtils.getLabelTextWidth(label) < list.getWidth();
        label.setToolTipText(fits ? null : label.getText());
        return label;
    }


}
