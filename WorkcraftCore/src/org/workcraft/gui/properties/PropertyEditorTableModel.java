package org.workcraft.gui.properties;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@SuppressWarnings("serial")
public class PropertyEditorTableModel extends AbstractTableModel {
    static final String[] columnNames = {"property", "value" };
    PropertyDescriptor[] declarations = null;
    PropertyClass[] rowClasses = null;

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void assign(Properties properties) {
        if (properties == null) {
            clear();
            return;
        }

        declarations = properties.getDescriptors().toArray(new PropertyDescriptor[0]);
        rowClasses = new PropertyClass[declarations.length];

        fireTableDataChanged();
        fireTableStructureChanged();
    }

    public void clear() {
        declarations = null;
        rowClasses = null;

        fireTableDataChanged();
        fireTableStructureChanged();
    }

    public void setRowClass(int i, PropertyClass cls) {
        rowClasses[i] = cls;
    }

    public int getColumnCount() {
        if (declarations == null) {
            return 0;
        } else {
            return 2;
        }
    }

    public int getRowCount() {
        if (declarations == null) {
            return 0;
        } else {
            return declarations.length;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col > 0) && declarations[row].isEditable();
    }

    public PropertyDescriptor getRowDeclaration(int i) {
        return declarations[i];
    }

    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return declarations[row].getName();
        } else {
            try {
                Object value = declarations[row].getValue();
                if (rowClasses[row] != null) {
                    return rowClasses[row].toCellRendererValue(value);
                } else {
                    Map<? extends Object, String> choice = declarations[row].getChoice();
                    if (choice != null) {
                        return choice.get(value);
                    } else {
                        return value.toString();
                    }
                }
            } catch (Throwable e) {
                return "#EXCEPTION";
            }
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        PropertyDescriptor desc = declarations[row];
        if (rowClasses[row] != null) {
            value = rowClasses[row].fromCellEditorValue(value);
        }
        try {
            desc.setValue(value);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause().getMessage(), e);
        }
    }

}
