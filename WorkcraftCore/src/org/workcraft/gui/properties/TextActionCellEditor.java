package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

@SuppressWarnings("serial")
public class TextActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextField textField  = new JTextField();
    private final JButton actionButton = new JButton();

    public TextActionCellEditor() {
        textField.setFocusable(true);
        textField.setBorder(SizeHelper.getTableCellBorder());
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    stopCellEditing();
                }
            }
        });

        actionButton.setFocusable(false);
        actionButton.setMargin(PropertyHelper.BUTTON_INSETS);


        panel.add(textField, BorderLayout.CENTER);
        panel.add(actionButton, BorderLayout.EAST);
        panel.setFocusable(false);
    }

    @Override
    public TextAction getCellEditorValue() {
        return new TextAction(textField.getText(), null);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof TextAction) {
            TextAction textAction = (TextAction) value;

            textField.setFont(table.getFont());
            textField.setText(textAction.getText());

            Action action = textAction.getAction();
            actionButton.setText(action.getText());
            actionButton.addActionListener(e -> {
                action.run();
                fireEditingStopped();
            });
        }
        return panel;
    }

}
