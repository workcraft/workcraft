package org.workcraft.gui.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * The <code>Ruler</code> is used to displays a ruler-style header for the viewport.
 */
public class Ruler implements GridListener {
    private Color background = new Color(225, 231, 242);
    private Font font;
    private Color foreground = new Color(0, 0, 0);
    private String[] horizontalMajorCaptions;
    private int[] horizontalMajorTicks;
    private int[] horizontalMinorTicks;
    private final int size;
    private final int majorTickSize;
    private final int minorTickSize;
    private final Rectangle shape;
    private String[] verticalMajorCaptions;
    private int[] verticalMajorTicks;
    private int[] verticalMinorTicks;

    /**
     * Constructs a new ruler with the default parameters:
     * <ul>
     * <li> Ruler size = 15 pixels;
     * <li> Major tick size = 10 pixels;
     * <li> Minor tick size = 3 pixels;
     * <li> 10pt SansSerif font to display coordinates.
     * </ul>
     */
    public Ruler(int size) {
        shape = new Rectangle();
        horizontalMinorTicks = new int[0];
        horizontalMajorTicks = new int[0];
        verticalMinorTicks = new int[0];
        verticalMajorTicks = new int[0];
        this.size = size;
        majorTickSize = (int) Math.round(size * 0.6);
        minorTickSize = (int) Math.round(size * 0.2);
        font = new Font(Font.SANS_SERIF, 0, (int) Math.round(0.6 * size));
    }

    public void draw(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(foreground);
        g.setBackground(background);
        g.setFont(font);
        int d = (int) Math.round(0.2 * size);

        // horizontal ruler
        g.clearRect(shape.x + size, shape.y, shape.width -  size, size);
        g.drawLine(shape.x, size, shape.width, size);
        if (minorTickSize > 0) {
            for (int t : horizontalMinorTicks) {
                g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y - minorTickSize);
            }
        }
        for (int i = 0; i < horizontalMajorTicks.length; i++) {
            int t = horizontalMajorTicks[i];
            g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y    - majorTickSize);
            g.drawString(horizontalMajorCaptions[i], t + shape.x + d, size + shape.y - d);
        }

        // vertical ruler
        g.clearRect(shape.x, shape.y + size, size, shape.height - size);
        g.drawLine(size, shape.y, size, shape.height);
        if (minorTickSize > 0) {
            for (int t : verticalMinorTicks) {
                g.drawLine(shape.x + size, t + shape.y, shape.x + size - minorTickSize, t + shape.y);
            }
        }
        for (int i = 0; i < verticalMajorTicks.length; i++) {
            int t = verticalMajorTicks[i];
            g.drawLine(shape.x + size, t + shape.y, shape.x + size - majorTickSize, t + shape.y);
            AffineTransform re = g.getTransform();
            g.translate(shape.x + size - d, shape.y + t - d);
            g.rotate(-Math.PI / 2);
            g.drawString(verticalMajorCaptions[i], 0, 0);
            g.setTransform(re);
        }

        // corner
        g.clearRect(shape.x, shape.y, size, size);
        g.drawLine(shape.x, shape.y + size, shape.x + size, shape.y + size);
        g.drawLine(shape.x + size, shape.y, shape.x + size, shape.y + size);
    }

    /**
     * @return The current background color of the ruler area
     */
    public Color getBackground() {
        return background;
    }

    /**
     * @return The font that is currently used to display coordinates
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return The current foreground color of the ruler
     */
    public Color getForeground() {
        return foreground;
    }

    /**
     * @return The current major tick size, in pixels
     */
    public int getMajorTickSize() {
        return majorTickSize;
    }

    /**
     * @return The current minor tick size, in pixels
     */
    public int getMinorTickSize() {
        return minorTickSize;
    }

    public Rectangle getShape() {
        return new Rectangle(shape);
    }

    /**
     * @return The size, in pixels, of the painted ruler area
     */
    public int getSize() {
        return size;
    }

    @Override
    public void gridChanged(Grid grid) {
        int[][] minorLinesScreen = grid.getMinorScreenPositions();
        horizontalMinorTicks = minorLinesScreen[0];
        verticalMinorTicks = minorLinesScreen[1];
        int[][] majorLinesScreen = grid.getMajorScreenPositions();
        horizontalMajorTicks = majorLinesScreen[0];
        verticalMajorTicks = majorLinesScreen[1];

        double[][] majorLines = grid.getMajorPositions();
        horizontalMajorCaptions = new String[majorLines[0].length];
        verticalMajorCaptions = new String[majorLines[1].length];

        for (int i = 0; i < majorLines[0].length; i++) {
            horizontalMajorCaptions[i] = String.format("%.2f", majorLines[0][i]);
        }
        for (int i = 0; i < majorLines[1].length; i++) {
            verticalMajorCaptions[i] = String.format("%.2f", majorLines[1][i]);
        }

    }

    /**
     * Set the background color of the ruler area.
     *
     * @param background
     *            The new background color
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * Set the font that is used to display coordinates.
     *
     * @param font
     *            The new font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Set the foreground color of the ruler. This color is used to draw ruler
     * ticks and coordinates.
     *
     * @param foreground
     *            The new foreground color
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setShape(int x, int y, int width, int height) {
        shape.setBounds(x, y, width, height);
    }

}
