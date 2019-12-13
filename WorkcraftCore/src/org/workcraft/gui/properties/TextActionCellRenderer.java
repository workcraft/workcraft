package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class TextActionCellRenderer extends JPanel implements TableCellRenderer {

    private final JTextField text = new JTextField();
    private final JButton button = new JButton();

    public TextActionCellRenderer() {
        text.setFocusable(true);
        text.setBorder(SizeHelper.getTableCellBorder());

        button.setFocusable(false);
        button.setMargin(PropertyHelper.BUTTON_INSETS);

        setLayout(new BorderLayout());
        add(text, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof TextAction) {
            TextAction textAction = (TextAction) value;

            text.setFont(table.getFont());
            text.setText(textAction.getText());

            Action action = textAction.getAction();
            if (action != null) {
                button.setText(action.getTitle());
                button.setToolTipText(ActionUtils.getActionTooltip(action));
            }
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if ((event != null) && (event.getX() > button.getX())) {
            return button.getToolTipText(event);
        }
        return super.getToolTipText(event);
    }

}
