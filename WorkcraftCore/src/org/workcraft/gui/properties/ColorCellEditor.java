package org.workcraft.gui.properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class ColorCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private final FlatComboBox comboBox;
    private final Color[] colors = {
            Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.LIGHT_GRAY, Color.WHITE,
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.YELLOW, Color.MAGENTA,
            Color.ORANGE, Color.PINK,
            };

    class ColorCellRenderer implements ListCellRenderer {

        private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(
                    list, " ", index, isSelected, cellHasFocus);

            if (value instanceof Color) {
                renderer.setBackground((Color) value);
            }
            return renderer;
        }
    }

    public ColorCellEditor() {
        comboBox = new FlatComboBox();
        for (Color color: colors) {
            comboBox.addItem(color);
        }
        comboBox.setEditable(true);
        comboBox.setFocusable(false);
        comboBox.setRenderer(new ColorCellRenderer());

        Color color = (Color) comboBox.getSelectedItem();
        comboBox.setEditor(new ColorComboBoxEditor(color));
        comboBox.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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
