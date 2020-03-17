package org.workcraft.gui.tools;

import org.workcraft.types.Func;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public final class BasicTableModel<T> extends AbstractTableModel {

    private final String title;
    private final List<T> items;
    private final Func<T, String> converter;

    public BasicTableModel(String title, List<T> items) {
        this(title, items, item -> item.toString());
    }

    public BasicTableModel(String title, List<T> items, Func<T, String> converter) {
        this.title = title;
        this.items = items;
        this.converter = converter;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        return (column == 0) ? title : null;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if ((row >= 0) && (row < items.size())) {
            return converter.eval(items.get(row));
        }
        return null;
    }

}
