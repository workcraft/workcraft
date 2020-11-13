package org.workcraft.gui.controls;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

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
        setBorder(GuiUtils.getTableCellBorder());
        return this;
    }

}
