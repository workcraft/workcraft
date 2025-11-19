package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class ActionListCellEditor extends AbstractCellEditor implements TableCellEditor {

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JPanel result = new JPanel(new GridLayout());
        result.setFocusable(false);
        if (value instanceof ActionList actions) {
            for (Action action : actions) {
                JButton button = new JButton(action.getTitle());
                button.setFocusable(false);
                button.setMargin(PropertyHelper.BUTTON_INSETS);
                button.setToolTipText(ActionUtils.getActionTooltip(action));
                button.setEnabled(action.isEnabled());
                button.addActionListener(e -> action.run());
                result.add(button);
            }
        }
        return result;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

}
