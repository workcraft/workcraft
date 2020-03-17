package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class PropertyEditorTableUI extends BasicTableUI {

    private final PropertyEditorTableModel model;

    public PropertyEditorTableUI(PropertyEditorTableModel model) {
        this.model = model;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (table instanceof PropertyEditorTable) {
            Rectangle clip = g.getClipBounds();
            Rectangle bounds = table.getBounds();
            bounds.x = 0;
            bounds.y = 0;
            if ((table.getRowCount() > 0) && (table.getColumnCount() > 0) && bounds.intersects(clip)) {
                Point minCell = ((PropertyEditorTable) table).getClipMinCell(clip);
                Point maxCell = ((PropertyEditorTable) table).getClipMaxCell(clip);
                paintGrid(g, minCell.y, maxCell.y, minCell.x, maxCell.x);
                paintCells(g, minCell.y, maxCell.y, minCell.x, maxCell.x);
            }
        } else {
            super.paint(g, c);
        }
    }

    private void paintGrid(Graphics g, int minRow, int maxRow, int minColumn, int maxColumn) {
        g.setColor(table.getGridColor());

        Rectangle minCellBound = table.getCellRect(minRow, minColumn, true);
        Rectangle maxCellBound = table.getCellRect(maxRow, maxColumn, true);
        Rectangle rect = minCellBound.union(maxCellBound);

        int w = rect.x + rect.width;
        int y = rect.y;
        for (int row = minRow; row <= maxRow; row++) {
            int rowHeight = table.getRowHeight(row);
            y += rowHeight;
            if (table.getShowHorizontalLines()) {
                g.drawLine(rect.x, y - 1, w - 1, y - 1);
            }
            PropertyDescriptor declaration = model.getDeclaration(row);
            if (table.getShowVerticalLines() && (declaration != null) && !declaration.isSpan()) {
                TableColumnModel columnModel = table.getColumnModel();
                int x = rect.x;
                for (int column = minColumn; column <= maxColumn; column++) {
                    int columnWidth = columnModel.getColumn(column).getWidth();
                    x += columnWidth;
                    g.drawLine(x - 1, y - rowHeight, x - 1, y - 1);
                }
            }
        }
    }

    private void paintCells(Graphics g, int minRow, int maxRow, int minColumn, int maxColumn) {
        for (int row = minRow; row <= maxRow; row++) {
            for (int column = minColumn; column <= maxColumn; column++) {
                int verticalMargin = table.getRowMargin();
                int horizontalMargin = table.getColumnModel().getColumnMargin();
                Rectangle rect = table.getCellRect(row, column, true);
                rect.setBounds(rect.x + horizontalMargin / 2, rect.y + verticalMargin / 2,
                        rect.width - horizontalMargin, rect.height - verticalMargin);

                if (table.isEditing() && (row == table.getEditingRow()) && (column == table.getEditingColumn())) {
                    paintCellEditor(rect);
                } else {
                    paintCellRenderer(g, row, column, rect);
                }
            }
        }
    }

    private void paintCellEditor(Rectangle rect) {
        Component component = table.getEditorComponent();
        if (component != null) {
            component.setBounds(rect);
            component.validate();
        }
    }

    private void paintCellRenderer(Graphics g, int row, int column, Rectangle rect) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        if (renderer != null) {
            rendererPane.removeAll();
            Component component = table.prepareRenderer(renderer, row, column);
            rendererPane.paintComponent(g, component, table, rect.x, rect.y, rect.width, rect.height, true);
        }
    }

}
