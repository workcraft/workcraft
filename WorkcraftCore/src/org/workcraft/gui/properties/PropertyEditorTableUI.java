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
        Rectangle clip = g.getClipBounds();
        Rectangle bounds = table.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        if ((table.getRowCount() > 0) && (table.getColumnCount() > 0) && bounds.intersects(clip)) {
            Point upperLeft = clip.getLocation();
            Point lowerRight = new Point(clip.x + clip.width - 1, clip.y + clip.height - 1);
            int minRow = table.rowAtPoint(upperLeft);
            if (minRow == -1) {
                minRow = 0;
            }
            int maxRow = table.rowAtPoint(lowerRight);
            if (maxRow == -1) {
                maxRow = table.getRowCount() - 1;
            }
            int minColumn = table.columnAtPoint(upperLeft);
            if (minColumn == -1) {
                minColumn = 0;
            }
            int maxColumn = table.columnAtPoint(lowerRight);
            if (maxColumn == -1) {
                maxColumn = table.getColumnCount() - 1;
            }
            paintGrid(g, minRow, maxRow, minColumn, maxColumn);
            paintCells(g, minRow, maxRow, minColumn, maxColumn);
        }
    }

    private void paintGrid(Graphics g, int minRow, int maxRow, int minColumn, int maxColumn) {
        g.setColor(table.getGridColor());

        Rectangle minCell = table.getCellRect(minRow, minColumn, true);
        Rectangle maxCell = table.getCellRect(maxRow, maxColumn, true);
        Rectangle rect = minCell.union(maxCell);

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
                    Component component = table.getEditorComponent();
                    component.setBounds(rect);
                    component.validate();
                } else {
                    TableCellRenderer renderer = table.getCellRenderer(row, column);
                    Component component = table.prepareRenderer(renderer, row, column);
                    if (component.getParent() == null) {
                        rendererPane.add(component);
                    }
                    rendererPane.paintComponent(g, component, table, rect.x, rect.y, rect.width, rect.height, true);
                }
            }
        }
    }

}
