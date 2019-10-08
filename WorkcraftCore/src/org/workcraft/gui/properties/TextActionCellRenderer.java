package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class TextActionCellRenderer extends JPanel implements TableCellRenderer {

    private final JTextField textField  = new JTextField();
    private final JButton actionButton = new JButton();

    public TextActionCellRenderer() {
        textField.setFocusable(true);
        textField.setBorder(SizeHelper.getTableCellBorder());

        actionButton.setFocusable(false);
        actionButton.setMargin(PropertyUtils.BUTTON_INSETS);

        setLayout(new BorderLayout());
        add(textField, BorderLayout.CENTER);
        add(actionButton, BorderLayout.EAST);
        setFocusable(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof TextAction) {
            TextAction composition = (TextAction) value;

            textField.setFont(table.getFont());
            textField.setText(composition.getText());

            Action action = composition.getAction();
            actionButton.setText(action.getText());
        }
        return this;
    }

}
