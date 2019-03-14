package org.workcraft.gui.tools;

import org.workcraft.types.Pair;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public final class ColorLegendTableModel extends AbstractTableModel {

    private static final int LEGEND_COLUMN_COLOR = 0;
    private static final int LEGEND_COLUMN_DESCRIPTION = 1;

    private final List<Pair<Color, String>> items;

    public ColorLegendTableModel(List<Pair<Color, String>> items) {
        this.items = items;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Class<?> getColumnClass(final int col) {
        switch (col) {
        case LEGEND_COLUMN_COLOR:
            return Color.class;
        case LEGEND_COLUMN_DESCRIPTION:
            return String.class;
        default:
            return null;
        }
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if ((row >= 0) && (row < items.size())) {
            Pair<Color, String> item = items.get(row);
            return (col == LEGEND_COLUMN_COLOR) ? item.getFirst() : item.getSecond();
        }
        return null;
    }

}
