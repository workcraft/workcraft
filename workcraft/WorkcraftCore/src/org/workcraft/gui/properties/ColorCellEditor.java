package org.workcraft.gui.properties;

import org.workcraft.gui.controls.FlatComboBox;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private static final Color[] PALETTE_COLORS = {
            Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.YELLOW, Color.MAGENTA,
            Color.ORANGE, Color.PINK, new Color(0, 0, 0, 0),
    };

    private final FlatComboBox comboBox;
    private final ColorComboBoxEditor colorEditor;

    // Table this editor was last activated in
    private JTable table = null;

    public ColorCellEditor() {
        comboBox = new FlatComboBox();
        for (Color color: PALETTE_COLORS) {
            comboBox.addItem(color);
        }
        comboBox.setEditable(true);
        comboBox.setFocusable(false);
        comboBox.setRenderer(new ColorComboBoxRenderer());

        Color color = (Color) comboBox.getSelectedItem();
        colorEditor = new ColorComboBoxEditor(color);
        colorEditor.setDialogDismissedHandler(this::cancelEditingIfNoChoice);
        comboBox.setEditor(colorEditor);
        comboBox.addActionListener(this);
        // Arrow keys move the highlight in the list without changing the selected item (as in DefaultCellEditor)
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        // The combo box is not focusable, but when the list is opened the focus goes to the editor button
        // (it is editable), so the list can be navigated by keyboard
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                cancelEditingIfNoChoice();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                cancelEditingIfNoChoice();
            }
        });
    }

    private void cancelEditingIfNoChoice() {
        // Deferred, as the item (if any) is chosen while the list is being closed. If the edit has been stopped
        // (item chosen) or replaced (another cell clicked), there is nothing to do. Otherwise list was dismissed
        // (e.g. by clicking on canvas), so the edit would hang and block updates of the property editor.
        SwingUtilities.invokeLater(() -> {
            // Colour dialog closes the list when it takes focus, but its result is still to be applied
            if (!colorEditor.isDialogOpen()) {
                PropertyEditorTable.cancelEditingIfActive(table, this);
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        comboBox.setSelectedItem(value);
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        fireEditingStopped();
    }

}
