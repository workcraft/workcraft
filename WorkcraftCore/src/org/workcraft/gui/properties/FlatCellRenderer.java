package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class FlatCellRenderer extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value == null) {
            setText("");
            setOpaque(true);
        } else {
            String text = value.toString();
            setText(text);
            setFont(table.getFont());
            setOpaque(text.isEmpty());
        }
        setBorder(SizeHelper.getTableCellBorder());
        return this;
    }

}
