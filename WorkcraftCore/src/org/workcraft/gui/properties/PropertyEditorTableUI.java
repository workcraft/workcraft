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
            int rMin = table.rowAtPoint(upperLeft);
            if (rMin == -1) {
                rMin = 0;
            }
            int rMax = table.rowAtPoint(lowerRight);
            if (rMax == -1) {
                rMax = table.getRowCount() - 1;
            }
            int cMin = table.columnAtPoint(upperLeft);
            if (cMin == -1) {
                cMin = 0;
            }
            int cMax = table.columnAtPoint(lowerRight);
            if (cMax == -1) {
                cMax = table.getColumnCount() - 1;
            }
            paintGrid(g, rMin, rMax, cMin, cMax);
            paintCells(g, rMin, rMax, cMin, cMax);
        }
    }

    private void paintGrid(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        g.setColor(table.getGridColor());

        Rectangle minCell = table.getCellRect(rMin, cMin, true);
        Rectangle maxCell = table.getCellRect(rMax, cMax, true);
        Rectangle rect = minCell.union(maxCell);

        int w = rect.x + rect.width;
        int y = rect.y;
        for (int row = rMin; row <= rMax; row++) {
            int rowHeight = table.getRowHeight(row);
            y += rowHeight;
            if (table.getShowHorizontalLines()) {
                g.drawLine(rect.x, y - 1, w - 1, y - 1);
            }
            if (table.getShowVerticalLines() && !model.getRowDeclaration(row).isSpan()) {
                TableColumnModel cm = table.getColumnModel();
                int x = rect.x;
                for (int column = cMin; column <= cMax; column++) {
                    int columnWidth = cm.getColumn(column).getWidth();
                    x += columnWidth;
                    g.drawLine(x - 1, y - rowHeight, x - 1, y - 1);
                }
            }
        }
    }

    private void paintCells(Graphics g, int rMin, int rMax, int cMin, int cMax) {
        for (int row = rMin; row <= rMax; row++) {
            for (int column = cMin; column <= cMax; column++) {
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
