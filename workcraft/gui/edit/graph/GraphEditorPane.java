package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Color;
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

import org.workcraft.dom.visual.VisualAbstractGraphModel;

public class GraphEditorPane extends JPanel implements ComponentListener, MouseMotionListener, MouseListener, MouseWheelListener{
	private static final long serialVersionUID = 1L;

	protected VisualAbstractGraphModel document;

	protected Viewport view;
	protected Grid grid;
	protected Ruler ruler;

	protected boolean panDrag = false;
	protected Point lastMouseCoords = new Point();

	protected Color background = Color.WHITE;

	public GraphEditorPane(VisualAbstractGraphModel document) {
		this.document = document;
		view = new Viewport(0, 0, this.getWidth(), this.getHeight());
		grid = new Grid();
		ruler = new Ruler();
		view.addListener(grid);
		grid.addListener(ruler);
		this.addComponentListener(this);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
	}


	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setBackground(background);

		g2d.clearRect(0, 0, getWidth(), getHeight());

		grid.draw(g2d);

		AffineTransform rest = g2d.getTransform();

		g2d.transform(view.getTransform());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		document.getRoot().draw(g2d);

		g2d.setTransform(rest);
		ruler.draw(g2d);
	}


	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}


	@Override
	public void componentResized(ComponentEvent e) {
		view.setShape(15, 15, this.getWidth()-15, this.getHeight()-15);
		ruler.setShape(0, 0, this.getWidth(), this.getHeight());
		repaint();
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

	public void setDocument(VisualAbstractGraphModel document) {

	}

}
