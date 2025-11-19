package org.workcraft.gui.properties;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.Path2D;

public class HandleCellRenderer extends JLabel implements TableCellRenderer {

    private static final Path2D SHAPE = new Path2D.Double();

    static {
        double dy = 1.5;
        double w2 = 5.5;
        double tw2 = 2.5;
        double dt = 3.0;
        SHAPE.moveTo(-tw2, -dt);
        SHAPE.lineTo(0.0, -dt - tw2);
        SHAPE.lineTo(tw2, -dt);
        SHAPE.closePath();

        SHAPE.moveTo(-w2, -dy);
        SHAPE.lineTo(w2, -dy);
        SHAPE.moveTo(-w2, 0.0);
        SHAPE.lineTo(w2, 0.0);
        SHAPE.moveTo(-w2, dy);
        SHAPE.lineTo(w2, dy);

        SHAPE.moveTo(-tw2, dt);
        SHAPE.lineTo(0.0,  dt + tw2);
        SHAPE.lineTo(tw2, dt);
        SHAPE.closePath();
    }

    public HandleCellRenderer() {
        super();
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (g instanceof Graphics2D g2) {
            g.setColor(Color.GRAY);
            g.translate(getWidth() / 2, getHeight() / 2);
            g2.draw(SHAPE);
        }
    }

}
