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
		this.x = new double[N];
		this.y = new double[N];
		this.speed = new double[N];

		this.dirx = new double[N];
		this.diry = new double[N];


		for (int i=0; i<N; i++) {
			this.x[i] = Math.random() * (getWidth()-width) + width;
			this.y[i] = Math.random() * (getHeight()-height) + height;

			this.dirx[i] = (Math.random() > 0.5) ? 1.0 : -1.0;
			this.diry[i] = (Math.random() > 0.5) ? 1.0 : -1.0;

			this.speed[i] = Math.random() * 10;
		}
	}

	public Java2DCanvas() {
		super();
		createRects();
	}

	protected void updateRects() {
		for (int i=0; i<N; i++) {

			if ( (this.x[i] + width) > getWidth() )
				this.dirx[i] *= -1;

			if ( (this.y[i] + height) > getHeight() )
				this.diry[i] *= -1;

			if ( (this.x[i]) < 0 )
				this.dirx[i] *= -1;

			if ( (this.y[i] ) < 0 )
				this.diry[i] *= -1;


			this.x[i] += this.speed[i]*this.dirx[i];
			this.y[i] += this.speed[i]*this.diry[i];

		}
	}



	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2d = (Graphics2D)g;

		Rectangle2D rect = new Rectangle2D.Double(0,0, getWidth(), getHeight());

		g2d.setPaint(Color.DARK_GRAY);
		g2d.fill(rect);


		updateRects();


		for (int i=0; i<N; i++) {
			rect.setRect(this.x[i], this.y[i], width, height);

			g2d.setColor(Color.RED);
			g2d.fill(rect);
			g2d.setColor(Color.BLACK);
			g2d.draw(rect);
		}

		repaint();
	}

	private static final long serialVersionUID = 1L;


}
