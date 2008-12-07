package org.workcraft.gui.edit.graph;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.SelectionTool;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class GraphEditorPane extends JPanel implements ComponentListener, MouseMotionListener, MouseListener, MouseWheelListener{
	private static final long serialVersionUID = 1L;

	protected VisualModel document;

	protected Viewport view;
	protected Grid grid;
	protected Ruler ruler;

	protected GraphEditorTool currentTool = new SelectionTool(); // TODO shound not be here

	protected boolean panDrag = false;
	protected Point lastMouseCoords = new Point();

	protected Color background = Color.WHITE;

	public GraphEditorPane(VisualModel document) {
		setDocument(document);
		this.view = new Viewport(0, 0, getWidth(), getHeight());
		this.grid = new Grid();

		this.ruler = new Ruler();
		this.view.addListener(this.grid);
		this.grid.addListener(this.ruler);
		addComponentListener(this);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
	}



	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setBackground(this.background);

		g2d.clearRect(0, 0, getWidth(), getHeight());

		this.grid.draw(g2d);

		AffineTransform rest = g2d.getTransform();

		g2d.transform(this.view.getTransform());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		this.document.draw(g2d);
		this.currentTool.draw(this, g2d);

		g2d.setTransform(rest);
		this.ruler.draw(g2d);
	}



	public void componentHidden(ComponentEvent e) {
	}


	public void componentMoved(ComponentEvent e) {
	}



	public void componentResized(ComponentEvent e) {
		this.view.setShape(15, 15, getWidth()-15, getHeight()-15);
		this.ruler.setShape(0, 0, getWidth(), getHeight());
		repaint();
	}



	public void componentShown(ComponentEvent e) {
	}


	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}



	public void mouseMoved(MouseEvent e) {
		Point currentMouseCoords = e.getPoint();
		if (this.panDrag) {
			this.view.pan(currentMouseCoords.x - this.lastMouseCoords.x, currentMouseCoords.y - this.lastMouseCoords.y);
			repaint();
		} else
			this.currentTool.mouseMoved(new GraphEditorMouseEvent(this.document, e));
		this.lastMouseCoords = currentMouseCoords;
	}



	public void mouseClicked(MouseEvent e) {
		if(e.getButton()!=MouseEvent.BUTTON2)
			this.currentTool.mouseClicked(new GraphEditorMouseEvent(this.document, e));
	}



	public void mouseEntered(MouseEvent e) {
		this.currentTool.mouseEntered(new GraphEditorMouseEvent(this.document, e));
	}



	public void mouseExited(MouseEvent e) {
		this.currentTool.mouseExited(new GraphEditorMouseEvent(this.document, e));
	}



	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			this.panDrag = true;
		else
			this.currentTool.mousePressed(new GraphEditorMouseEvent(this.document, e));
	}



	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			this.panDrag = false;
		else
			this.currentTool.mouseReleased(new GraphEditorMouseEvent(this.document, e));
	}



	public void mouseWheelMoved(MouseWheelEvent e) {
		this.view.zoom(-e.getWheelRotation(), e.getPoint());
		repaint();
	}

	public void setDocument(VisualModel document) {
		this.document = document;
		if(document.getEditorPane()!=this)
			document.setEditorPane(this);
	}

	public VisualModel getDocument() {
		return this.document;
	}

	public Viewport getViewport() {
		return this.view;
	}

	public void snap(Point2D point) {
		point.setLocation(this.grid.snapCoordinate(point.getX()), this.grid.snapCoordinate(point.getY()));
	}

}
