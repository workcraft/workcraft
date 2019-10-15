package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class ActionListCellRenderer extends JPanel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof ActionList) {
            ActionList actions = (ActionList) value;
            setLayout(new GridLayout(1, actions.size()));
            for (Action action : actions) {
                add(new JButton(action.getText()));
            }
        }
        return this;
    }

}
