package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ActionCellRenderer extends JButton implements TableCellRenderer {

    public ActionCellRenderer() {
        super();
        setMargin(PropertyHelper.BUTTON_INSETS);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Action action) {
            setText(action.getTitle());
        }
        return this;
    }

}
