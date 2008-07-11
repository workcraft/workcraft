package org.workcraft.gui.edit.work;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

public class Ruler implements GridListener {

//	public class Tick {
//		public int position;
////		public int thickness;
//		public int length;
//		public Color color;
//		public String caption;
//	}

	protected int size = 15;
	protected int minorTickSize = 1;
	protected int majorTickSize = 10;

	protected Color background = new Color(220,235,235);
	protected Color foreground = new Color (0,0,0);
	protected Color majorLinesColor = new Color (200,200,200);
	protected Color minorLinesColor = new Color (240,240,240);


	protected Rectangle shape;

	protected int[] horizontalMinorTicks;
	protected int[] horizontalMajorTicks;
	protected int[] verticalMinorTicks;
	protected int[] verticalMajorTicks;

	protected String verticalMajorCaptions[];
	protected String horizontalMajorCaptions[];

	protected Font font;

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
		g.clearRect(shape.x, shape.y+size, size, shape.height - size );

		g.setColor(foreground);
		g.drawLine(shape.x, size, shape.width, size);
		g.drawLine(size, shape.y, size, shape.height);
	//	g.drawLine(shape.x, shape.y, shape.x + size, shape.y + size);

		g.setStroke(new BasicStroke(1f));

		for (int t : horizontalMinorTicks) {
		//	g.setColor(minorLinesColor);
			g.drawLine(t+shape.x, size+shape.y, t+shape.x, size+shape.y-minorTickSize);
		}

		for (int t : verticalMinorTicks) {
		//	g.setColor(minorLinesColor);
			g.drawLine(shape.x+size, t+shape.y, shape.x+size-minorTickSize, t+shape.y);
		}

		for (int i=0;i<horizontalMajorTicks.length; i++) {
			int t = horizontalMajorTicks[i];

		//	g.setColor(majorLinesColor);
			g.drawLine(t+shape.x, size+shape.y, t+shape.x, size+shape.y-majorTickSize);
			g.setColor(foreground);
			g.setFont(font);
			g.drawString(horizontalMajorCaptions[i], t+shape.x+2, size+shape.y-2);
		}

		for (int i=0;i<verticalMajorTicks.length; i++) {
			int t = verticalMajorTicks[i];
		//	g.setColor(majorLinesColor);
			g.drawLine(shape.x+size, t+shape.y, shape.x+size-majorTickSize, t+shape.y);
			g.setColor(foreground);
			g.setFont(font);
			//g.rotate(Math.PI/2);
			AffineTransform re = g.getTransform();

			g.translate(shape.x+size-2, shape.y+t-2);
			g.rotate(-Math.PI/2);
			g.drawString(verticalMajorCaptions[i],0,0);
			g.setTransform(re);

		}
	}

	public void setShape (int x, int y, int width, int height) {
		shape.setBounds(x, y, width, height);
	}


	public void setShape (Rectangle shape) {
		setShape(shape.x, shape.y, shape.width, shape.height);
	}

	public Rectangle getShape() {
		return new Rectangle(shape);
	}

	@Override
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

		for (int i=0; i<majorLines[0].length; i++)
			horizontalMajorCaptions[i] = String.format("%.2f", majorLines[0][i]);
		for (int i=0; i<majorLines[1].length; i++)
			verticalMajorCaptions[i] = String.format("%.2f", majorLines[1][i]);


	}
}