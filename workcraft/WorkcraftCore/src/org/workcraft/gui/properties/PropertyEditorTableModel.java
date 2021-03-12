package org.workcraft.gui.properties;

import javax.swing.table.AbstractTableModel;
import java.util.Map;

public class PropertyEditorTableModel extends AbstractTableModel {

    private final String[] columnNames;
    private PropertyDescriptor[] declarations = null;
    private PropertyClass[] rowClasses = null;

    public PropertyEditorTableModel(String propertyHeader, String valueHeader) {
        columnNames = new String[]{propertyHeader, valueHeader};
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void assign(Properties properties) {
        if (properties == null) {
            clear();
        } else {
            declarations = properties.getDescriptors().toArray(new PropertyDescriptor[0]);
            rowClasses = new PropertyClass[declarations.length];
            fireTableDataChanged();
            fireTableStructureChanged();
        }
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

    @Override
    public int getColumnCount() {
        if (declarations == null) {
            return 0;
        } else {
            return 2;
        }
    }

    @Override
    public int getRowCount() {
        if (declarations == null) {
            return 0;
        } else {
            return declarations.length;
        }
    }

    private boolean isRowInRange(int row) {
        return (row >= 0) && (row < getRowCount());
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col > 0) && isRowInRange(row) && declarations[row].isEditable();
    }

    public PropertyDescriptor getDeclaration(int row) {
        return isRowInRange(row) ? declarations[row] : null;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (!isRowInRange(row)) {
            return "#OUT_OF_RANGE";
        }
        if (col == 0) {
            return declarations[row].getName();
        }
        try {
            Object value = declarations[row].getValue();
            if (rowClasses[row] != null) {
                return rowClasses[row].toCellRendererValue(value);
            } else {
                Map<?, String> choice = declarations[row].getChoice();
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

    @Override
    public void setValueAt(Object value, int row, int col) {
        PropertyDescriptor declaration = declarations[row];
        if (rowClasses[row] != null) {
            value = rowClasses[row].fromCellEditorValue(value);
        }
        declaration.setValue(value);
    }

}
