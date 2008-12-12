package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * The <code>Ruler</code> is used to displays a ruler-style header for the
 * viewport.
 *
 * @author Ivan Poliakov
 *
 */
public class Ruler implements GridListener {
	protected Color background = new Color(225, 231, 242);
	protected Font font;
	protected Color foreground = new Color(0, 0, 0);
	protected String horizontalMajorCaptions[];
	protected int[] horizontalMajorTicks;
	protected int[] horizontalMinorTicks;
	protected Color majorLinesColor = new Color(200, 200, 200);
	protected int majorTickSize = 10;
	protected Color minorLinesColor = new Color(240, 240, 240);
	protected int minorTickSize = 3;
	protected Rectangle shape;
	protected int size = 15;
	protected String verticalMajorCaptions[];
	protected int[] verticalMajorTicks;
	protected int[] verticalMinorTicks;

	/**
	 * Constructs a new ruler with the default parameters:
	 * <ul>
	 * <li> Ruler size = 15 pixels;
	 * <li> Major tick size = 10 pixels;
	 * <li> Minor tick size = 3 pixels;
	 * <li> 10pt SansSerif font to display coordinates.
	 * </ul>
	 */
	public Ruler() {
		shape = new Rectangle();
		horizontalMinorTicks = new int[0];
		horizontalMajorTicks = new int[0];
		verticalMinorTicks = new int[0];
		verticalMajorTicks = new int[0];

		font = new Font(Font.SANS_SERIF, 0, 10);
	}

	public void draw(Graphics2D g) {
		g.setBackground(background);
		g.clearRect(shape.x, shape.y, shape.width, size);
		g.clearRect(shape.x, shape.y + size, size, shape.height - size);

		g.setColor(foreground);
		g.drawLine(shape.x, size, shape.width, size);
		g.drawLine(size, shape.y, size, shape.height);

		g.setStroke(new BasicStroke(1f));

		if (minorTickSize > 0) {
			for (int t : horizontalMinorTicks)
				g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y
						- minorTickSize);

			for (int t : verticalMinorTicks)
				g.drawLine(shape.x + size, t + shape.y, shape.x + size
						- minorTickSize, t + shape.y);
		}

		for (int i = 0; i < horizontalMajorTicks.length; i++) {
			int t = horizontalMajorTicks[i];
			g.drawLine(t + shape.x, size + shape.y, t + shape.x, size + shape.y
					- majorTickSize);
			g.setColor(foreground);
			g.setFont(font);
			g.drawString(horizontalMajorCaptions[i], t + shape.x + 2, size
					+ shape.y - 2);
		}

		for (int i = 0; i < verticalMajorTicks.length; i++) {
			int t = verticalMajorTicks[i];
			g.drawLine(shape.x + size, t + shape.y, shape.x + size
					- majorTickSize, t + shape.y);
			g.setColor(foreground);
			g.setFont(font);
			AffineTransform re = g.getTransform();

			g.translate(shape.x + size - 2, shape.y + t - 2);
			g.rotate(-Math.PI / 2);
			g.drawString(verticalMajorCaptions[i], 0, 0);
			g.setTransform(re);

		}
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


	public void gridChanged(Grid grid) {
		int[][] minorLinesScreen = grid.getMinorLinePositionsScreen();
		horizontalMinorTicks = minorLinesScreen[0];
		verticalMinorTicks = minorLinesScreen[1];
		int[][] majorLinesScreen = grid.getMajorLinePositionsScreen();
		horizontalMajorTicks = majorLinesScreen[0];
		verticalMajorTicks = majorLinesScreen[1];

		double[][] majorLines = grid.getMajorLinePositions();
		horizontalMajorCaptions = new String[majorLines[0].length];
		verticalMajorCaptions = new String[majorLines[1].length];

		for (int i = 0; i < majorLines[0].length; i++)
			horizontalMajorCaptions[i] = String
			.format("%.2f", majorLines[0][i]);
		for (int i = 0; i < majorLines[1].length; i++)
			verticalMajorCaptions[i] = String.format("%.2f", majorLines[1][i]);

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

	/**
	 * Set the size, in pixels, of major ruler ticks. Set to 0 to hide the major
	 * ticks.
	 */
	public void setMajorTickSize(int majorTickSize) {
		this.majorTickSize = majorTickSize;
	}

	/**
	 * Set the size, in pixels, of major ruler ticks. Set to 0 to hide the minor
	 * ticks.
	 */
	public void setMinorTickSize(int minorTickSize) {
		this.minorTickSize = minorTickSize;
	}

	public void setShape(int x, int y, int width, int height) {
		shape.setBounds(x, y, width, height);
	}

	public void setShape(Rectangle shape) {
		setShape(shape.x, shape.y, shape.width, shape.height);
	}

	/**
	 * Set the size, in pixels, of the painted ruler area. The sizes of vertical
	 * and horizontal rulers are the same.
	 *
	 * @param size
	 *            The new ruler size
	 */
	public void setSize(int size) {
		this.size = size;
	}
}