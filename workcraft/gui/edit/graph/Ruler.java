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
		this.shape = new Rectangle();
		this.horizontalMinorTicks = new int[0];
		this.horizontalMajorTicks = new int[0];
		this.verticalMinorTicks = new int[0];
		this.verticalMajorTicks = new int[0];

		this.font = new Font(Font.SANS_SERIF, 0, 10);
	}

	public void draw(Graphics2D g) {
		g.setBackground(this.background);
		g.clearRect(this.shape.x, this.shape.y, this.shape.width, this.size);
		g.clearRect(this.shape.x, this.shape.y + this.size, this.size, this.shape.height - this.size);

		g.setColor(this.foreground);
		g.drawLine(this.shape.x, this.size, this.shape.width, this.size);
		g.drawLine(this.size, this.shape.y, this.size, this.shape.height);

		g.setStroke(new BasicStroke(1f));

		if (this.minorTickSize > 0) {
			for (int t : this.horizontalMinorTicks)
				g.drawLine(t + this.shape.x, this.size + this.shape.y, t + this.shape.x, this.size + this.shape.y
						- this.minorTickSize);

			for (int t : this.verticalMinorTicks)
				g.drawLine(this.shape.x + this.size, t + this.shape.y, this.shape.x + this.size
						- this.minorTickSize, t + this.shape.y);
		}

		for (int i = 0; i < this.horizontalMajorTicks.length; i++) {
			int t = this.horizontalMajorTicks[i];
			g.drawLine(t + this.shape.x, this.size + this.shape.y, t + this.shape.x, this.size + this.shape.y
					- this.majorTickSize);
			g.setColor(this.foreground);
			g.setFont(this.font);
			g.drawString(this.horizontalMajorCaptions[i], t + this.shape.x + 2, this.size
					+ this.shape.y - 2);
		}

		for (int i = 0; i < this.verticalMajorTicks.length; i++) {
			int t = this.verticalMajorTicks[i];
			g.drawLine(this.shape.x + this.size, t + this.shape.y, this.shape.x + this.size
					- this.majorTickSize, t + this.shape.y);
			g.setColor(this.foreground);
			g.setFont(this.font);
			AffineTransform re = g.getTransform();

			g.translate(this.shape.x + this.size - 2, this.shape.y + t - 2);
			g.rotate(-Math.PI / 2);
			g.drawString(this.verticalMajorCaptions[i], 0, 0);
			g.setTransform(re);

		}
	}

	/**
	 * @return The current background color of the ruler area
	 */
	public Color getBackground() {
		return this.background;
	}

	/**
	 * @return The font that is currently used to display coordinates
	 */
	public Font getFont() {
		return this.font;
	}

	/**
	 * @return The current foreground color of the ruler
	 */
	public Color getForeground() {
		return this.foreground;
	}

	/**
	 * @return The current major tick size, in pixels
	 */
	public int getMajorTickSize() {
		return this.majorTickSize;
	}

	/**
	 * @return The current minor tick size, in pixels
	 */
	public int getMinorTickSize() {
		return this.minorTickSize;
	}

	public Rectangle getShape() {
		return new Rectangle(this.shape);
	}

	/**
	 * @return The size, in pixels, of the painted ruler area
	 */
	public int getSize() {
		return this.size;
	}


	public void gridChanged(Grid grid) {
		int[][] minorLinesScreen = grid.getMinorLinePositionsScreen();
		this.horizontalMinorTicks = minorLinesScreen[0];
		this.verticalMinorTicks = minorLinesScreen[1];
		int[][] majorLinesScreen = grid.getMajorLinePositionsScreen();
		this.horizontalMajorTicks = majorLinesScreen[0];
		this.verticalMajorTicks = majorLinesScreen[1];

		double[][] majorLines = grid.getMajorLinePositions();
		this.horizontalMajorCaptions = new String[majorLines[0].length];
		this.verticalMajorCaptions = new String[majorLines[1].length];

		for (int i = 0; i < majorLines[0].length; i++)
			this.horizontalMajorCaptions[i] = String
			.format("%.2f", majorLines[0][i]);
		for (int i = 0; i < majorLines[1].length; i++)
			this.verticalMajorCaptions[i] = String.format("%.2f", majorLines[1][i]);

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
		this.shape.setBounds(x, y, width, height);
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