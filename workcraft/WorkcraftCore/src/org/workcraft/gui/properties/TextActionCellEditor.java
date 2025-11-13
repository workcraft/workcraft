package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;
import org.workcraft.gui.controls.FlatTextField;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class TextActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JButton leftButton = new JButton();
    private final JTextField text = new FlatTextField();
    private final JButton rightButton = new JButton();

    public TextActionCellEditor() {
        leftButton.setFocusable(false);
        leftButton.setVisible(false);
        leftButton.setMargin(PropertyHelper.BUTTON_INSETS);

        text.setFocusable(true);
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    stopCellEditing();
                }
            }
        });

        rightButton.setFocusable(false);
        rightButton.setVisible(false);
        rightButton.setMargin(PropertyHelper.BUTTON_INSETS);

        panel.add(leftButton, BorderLayout.WEST);
        panel.add(text, BorderLayout.CENTER);
        panel.add(rightButton, BorderLayout.EAST);
        panel.setFocusable(false);
    }

    @Override
    public TextAction getCellEditorValue() {
        return new TextAction(text.getText());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof TextAction textAction) {
            int buttonSize = table.getRowHeight(row);
            Dimension buttonDimension = new Dimension(buttonSize, buttonSize);

            Action leftAction = textAction.getLeftAction();
            if (leftAction != null) {
                leftButton.setPreferredSize(buttonDimension);
                leftButton.setVisible(true);
                leftButton.setText(leftAction.getTitle());
                leftButton.setToolTipText(ActionUtils.getActionTooltip(leftAction));
                leftButton.addActionListener(e -> {
                    // Call fireEditingStopped() before model modifying action to save memento
                    if (textAction.isModelModifyingLeftAction()) {
                        fireEditingStopped();
                    }
                    leftAction.run();
                });
            }

            text.setFont(table.getFont());
            text.setText(textAction.getText());

            Color foreground = textAction.getForeground();
            if (foreground != null) {
                text.setForeground(foreground);
            }

            Color background = textAction.getBackground();
            if (background != null) {
                text.setBackground(background);
            }

            Action rightAction = textAction.getRightAction();
            if (rightAction != null) {
                rightButton.setPreferredSize(buttonDimension);
                rightButton.setVisible(true);
                rightButton.setText(rightAction.getTitle());
                rightButton.setToolTipText(ActionUtils.getActionTooltip(rightAction));
                rightButton.addActionListener(e -> {
                    // Call fireEditingStopped() before model modifying action to save memento
                    if (textAction.isModelModifyingRightAction()) {
                        fireEditingStopped();
                    }
                    rightAction.run();
                });
            }
        }
        return panel;
    }

}
