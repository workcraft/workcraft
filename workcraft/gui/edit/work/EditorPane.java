package org.workcraft.gui.edit.work;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

public class EditorPane extends JPanel implements ComponentListener, MouseMotionListener, MouseListener, MouseWheelListener{
	private static final long serialVersionUID = 1L;

	protected Viewport view;
	protected Grid grid;

	protected boolean panDrag = false;
	protected Point lastMouseCoords = new Point();

	public EditorPane() {
		view = new Viewport(0, 0, this.getWidth(), this.getHeight());
		grid = new Grid();
		this.addComponentListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
	}


	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.clearRect(0, 0, getWidth(), getHeight());
	//	g2d.setClip(0, 0, getWidth(), getHeight());
		AffineTransform rest = g2d.getTransform();


		g2d.transform(view.getTransform());

		grid.draw(g2d, view);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape qw = new java.awt.geom.Rectangle2D.Double( -0.5, -0.5, 1.0,1.0);



		g2d.setStroke(new BasicStroke(0.05f));


		for (int i=0; i<100; i++) {
			g2d.translate(0, 1.2);
			for (int j=0; j<100; j++) {
				g2d.translate(1.2, 0);
				g2d.draw(qw);
			}

			g2d.translate (-120, 0);
		}

		g2d.setTransform(rest);
	}


	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}


	@Override
	public void componentResized(ComponentEvent e) {
		view.setShape(0, 0,getWidth(), getHeight());
	}


	@Override
	public void componentShown(ComponentEvent e) {
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();

		if (panDrag) {

			view.pan(currentMouseCoords.x - lastMouseCoords.x, currentMouseCoords.y - lastMouseCoords.y);
			repaint();
		}

		lastMouseCoords = currentMouseCoords;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}


	@Override
	public void mouseExited(MouseEvent e) {
	}


	@Override
	public void mousePressed(MouseEvent e) {

		if (e.getButton() == MouseEvent.BUTTON1) {
			panDrag = true;
		}
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			panDrag = false;
		}
	}


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		view.zoom(-e.getWheelRotation(), e.getPoint());
		repaint();

	}
}
