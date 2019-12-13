package org.workcraft.gui.properties;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

@SuppressWarnings("serial")
public class TextActionCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextField text = new JTextField();
    private final JButton button = new JButton();

    public TextActionCellEditor() {
        text.setFocusable(true);
        text.setBorder(SizeHelper.getTableCellBorder());
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!e.isTemporary()) {
                    stopCellEditing();
                }
            }
        });

        button.setFocusable(false);
        button.setMargin(PropertyHelper.BUTTON_INSETS);


        panel.add(text, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        panel.setFocusable(false);
    }

    @Override
    public TextAction getCellEditorValue() {
        return new TextAction(text.getText(), null);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value instanceof TextAction) {
            TextAction textAction = (TextAction) value;

            text.setFont(table.getFont());
            text.setText(textAction.getText());

            Action action = textAction.getAction();
            button.setText(action.getTitle());
            button.setToolTipText(ActionUtils.getActionTooltip(action));
            button.addActionListener(e -> {
                action.run();
                fireEditingStopped();
            });
        }
        return panel;
    }

}
