package org.workcraft.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public class Java2DCanvas extends JPanel {
	static final int N = 100;
	static final double height = 50;
	static final double width = 50;

	double x[];
	double y[];
	double dirx[];
	double diry[];
	double speed[];

	protected void createRects() {
		x = new double[N];
		y = new double[N];
		speed = new double[N];

		dirx = new double[N];
		diry = new double[N];


		for (int i=0; i<N; i++) {
			x[i] = Math.random() * (this.getWidth()-width) + width;
			y[i] = Math.random() * (this.getHeight()-height) + height;

			dirx[i] = (Math.random() > 0.5) ? 1.0 : -1.0;
			diry[i] = (Math.random() > 0.5) ? 1.0 : -1.0;

			speed[i] = Math.random() * 10;
		}
	}

	public Java2DCanvas() {
		super();
		createRects();
	}

	protected void updateRects() {
		for (int i=0; i<N; i++) {

			if ( (x[i] + width) > this.getWidth() )
				dirx[i] *= -1;

			if ( (y[i] + height) > this.getHeight() )
				diry[i] *= -1;

			if ( (x[i]) < 0 )
				dirx[i] *= -1;

			if ( (y[i] ) < 0 )
				diry[i] *= -1;


			x[i] += speed[i]*dirx[i];
			y[i] += speed[i]*diry[i];

		}
	}



	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D)g;

		Rectangle2D rect = new Rectangle2D.Double(0,0, this.getWidth(), this.getHeight());

		g2d.setPaint(Color.DARK_GRAY);
		g2d.fill(rect);


		updateRects();


		for (int i=0; i<N; i++) {
			rect.setRect(x[i], y[i], width, height);

			g2d.setColor(Color.RED);
			g2d.fill(rect);
			g2d.setColor(Color.BLACK);
			g2d.draw(rect);
		}

		repaint();
	}

	private static final long serialVersionUID = 1L;


}
