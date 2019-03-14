package org.workcraft.gui.tools;

import org.workcraft.types.Func;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public final class BasicTableModel<T> extends AbstractTableModel {

    private final List<T> items;
    private final Func<T, String> converter;

    public BasicTableModel(List<T> items) {
        this(items, item -> item.toString());
    }

    public BasicTableModel(List<T> items, Func<T, String> converter) {
        this.items = items;
        this.converter = converter;
    }

    @Override
    public int getColumnCount() {
        return 1;
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
