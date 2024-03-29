package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;
import org.workcraft.gui.controls.FlatLabel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class BasicCellRenderer implements TableCellRenderer {

    private final JComponent component;

    public BasicCellRenderer() {
        this(false);
    }

    public BasicCellRenderer(boolean hasPredefinedValues) {
        component = hasPredefinedValues ? new FlatComboBox() : new FlatLabel();
    }

    private void setText(String text) {
        if (component instanceof FlatLabel) {
            ((FlatLabel) component).setText(text);
        }
        if (component instanceof FlatComboBox) {
            ((FlatComboBox) component).addItem(text);
        }
        component.setOpaque(text.isEmpty());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        setText(value == null ? "" : value.toString());
        component.setFont(table.getFont());
        component.setEnabled(table.isCellEditable(row, column));
        return component;
    }

}
