package org.workcraft.gui.propertyeditor;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class ColorCellRenderer extends JLabel implements TableCellRenderer {

    public ColorCellRenderer() {
        setOpaque(true); //MUST do this for background to show up.
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setBorder(PropertyEditorTable.BORDER_RENDER);
        if (value != null) {
            setBackground((Color) value);
        }
        return this;
    }
}
