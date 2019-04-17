package org.workcraft.gui.tools;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.types.Pair;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class ColorLegendTable extends JTable {

    private final class TableModel extends AbstractTableModel {

        private static final int LEGEND_COLUMN_COLOR = 0;
        private static final int LEGEND_COLUMN_DESCRIPTION = 1;

        private final List<Pair<Color, String>> items;

        TableModel(List<Pair<Color, String>> items) {
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

    public ColorLegendTable(List<Pair<Color, String>> items) {
        setModel(new TableModel(items));

        // Make the table non-editable
        setFocusable(false);
        setRowSelectionAllowed(false);
        setRowHeight(SizeHelper.getComponentHeightFromFont(getFont()));
        setDefaultRenderer(Color.class, new ColorDataRenderer());
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        // Make the table transparent
        setShowGrid(false);
        setOpaque(false);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) getDefaultRenderer(Object.class);
        renderer.setOpaque(false);

        // Set the color cells square shape
        TableColumnModel columnModel = getColumnModel();
        int colorCellSize = getRowHeight();
        TableColumn column = columnModel.getColumn(0);
        column.setMinWidth(colorCellSize);
        column.setMaxWidth(colorCellSize);
    }

}
