package org.workcraft.plugins.son.test;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

/** @see http://stackoverflow.com/a/10067560/230513 */
public class test extends JPanel {

    private NumberFormat nf = NumberFormat.getCurrencyInstance();

    public test() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Amount"}, 0) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Double.class;
            }
        };
        for (int i = 0; i < 16; i++) {
            model.addRow(new Object[]{Double.valueOf(i)});
        }
        JTable table = new JTable(model) {

            @Override // Always selectAll()
            public boolean editCellAt(int row, int column, EventObject e) {
                boolean result = super.editCellAt(row, column, e);
                final Component editor = getEditorComponent();
                if (editor == null || !(editor instanceof JTextComponent)) {
                    return result;
                }
                if (e instanceof MouseEvent) {
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ((JTextComponent) editor).selectAll();
                        }
                    });
                } else {
                    ((JTextComponent) editor).selectAll();
                }
                return result;
            }
        };
        table.setPreferredScrollableViewportSize(new Dimension(123, 123));
        table.setDefaultRenderer(Double.class, new CurrencyRenderer(nf));
        table.setDefaultEditor(Double.class, new CurrencyEditor(nf));
        this.add(new JScrollPane(table));
    }

    private static class CurrencyRenderer extends DefaultTableCellRenderer {

        private NumberFormat formatter;

        public CurrencyRenderer(NumberFormat formatter) {
            this.formatter = formatter;
            this.setHorizontalAlignment(JLabel.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : formatter.format(value));
        }
    }

    private static class CurrencyEditor extends DefaultCellEditor {

        private NumberFormat formatter;
        private JTextField textField;

        public CurrencyEditor(NumberFormat formatter) {
            super(new JTextField());
            this.formatter = formatter;
            this.textField = (JTextField) this.getComponent();
            textField.setHorizontalAlignment(JTextField.RIGHT);
            textField.setBorder(null);
        }

        @Override
        public Object getCellEditorValue() {
            try {
                return new Double(textField.getText());
            } catch (NumberFormatException e) {
                return Double.valueOf(0);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {
            textField.setText((value == null)
                ? "" : formatter.format((Double) value));
            return textField;
        }
    }

    private void display() {
        JFrame f = new JFrame("RenderEditNumber");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(this);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new test().display();
            }
        });
    }
}