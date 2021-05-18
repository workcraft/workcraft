package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class ActionListCellRenderer extends JPanel implements TableCellRenderer {

    private final Map<Action, JButton> actionButtonMap = new HashMap<>();

    public ActionListCellRenderer() {
        setLayout(new GridLayout());
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof ActionList) {
            ActionList actions = (ActionList) value;
            for (Action action : actions) {
                if (!actionButtonMap.containsKey(action)) {
                    JButton button = new JButton();
                    button.setText(action.getTitle());
                    button.setFocusable(false);
                    button.setMargin(PropertyHelper.BUTTON_INSETS);
                    button.setToolTipText(ActionUtils.getActionTooltip(action));
                    add(button);
                    actionButtonMap.put(action, button);
                }
            }
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (event != null) {
            int x = event.getX();
            for (JButton button : actionButtonMap.values()) {
                if ((x >= button.getX()) && (x < button.getX() + button.getWidth())) {
                    return button.getToolTipText(event);
                }
            }
        }
        return super.getToolTipText();
    }

}
