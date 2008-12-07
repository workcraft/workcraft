package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
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
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.SelectionTool;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class GraphEditorPane extends JPanel implements ComponentListener, MouseMotionListener, MouseListener, MouseWheelListener{
	private static final long serialVersionUID = 1L;

	protected VisualModel visualModel;

	protected MainWindow parent;

	protected Viewport view;
	protected Grid grid;
	protected Ruler ruler;

	protected boolean panDrag = false;
	protected boolean hasFocus = false;
	protected Point lastMouseCoords = new Point();

	protected Color background = Color.WHITE;
	protected Color focusBorderColor = Color.GRAY;
	protected Stroke borderStroke = new BasicStroke(2);

	public GraphEditorPane(MainWindow parent, VisualModel visualModel) {
		setModel(visualModel);

		this.parent = parent;

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

		this.visualModel.draw(g2d);
		parent.getToolboxView().getSelectedTool().drawInUserSpace(this, g2d);
		g2d.setTransform(rest);

		this.ruler.draw(g2d);
		parent.getToolboxView().getSelectedTool().drawInScreenSpace(this, g2d);

		if (hasFocus) {
			g2d.setStroke(borderStroke);
			g2d.setColor(focusBorderColor);
			g2d.drawRect(0, 0, this.getWidth()-1, this.getHeight()-1);
		}
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
			parent.getToolboxView().getSelectedTool().mouseMoved(new GraphEditorMouseEvent(this, e));
		this.lastMouseCoords = currentMouseCoords;
	}



	public void mouseClicked(MouseEvent e) {
		if (hasFocus) {
			if(e.getButton()!=MouseEvent.BUTTON2)
				parent.getToolboxView().getSelectedTool().mouseClicked(new GraphEditorMouseEvent(this, e));
		}	else
			if (e.getButton() == MouseEvent.BUTTON1)
				parent.requestFocus(this);
	}



	public void mouseEntered(MouseEvent e) {
		if (hasFocus)
			parent.getToolboxView().getSelectedTool().mouseEntered(new GraphEditorMouseEvent(this, e));
	}



	public void mouseExited(MouseEvent e) {
		if (hasFocus)
			parent.getToolboxView().getSelectedTool().mouseExited(new GraphEditorMouseEvent(this, e));
	}



	public void mousePressed(MouseEvent e) {
		if (hasFocus) {
			if (e.getButton() == MouseEvent.BUTTON2)
				this.panDrag = true;
			else
				parent.getToolboxView().getSelectedTool().mousePressed(new GraphEditorMouseEvent(this, e));
		}
	}



	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON2)
			this.panDrag = false;
		else
			parent.getToolboxView().getSelectedTool().mouseReleased(new GraphEditorMouseEvent(this, e));
	}



	public void mouseWheelMoved(MouseWheelEvent e) {
		this.view.zoom(-e.getWheelRotation(), e.getPoint());
		repaint();
	}

	public void setModel(VisualModel document) {
		this.visualModel = document;
		if(document.getEditorPane()!=this)
			document.setEditorPane(this);
	}

	public VisualModel getModel() {
		return this.visualModel;
	}

	public Viewport getViewport() {
		return this.view;
	}

	public void snap(Point2D point) {
		point.setLocation(this.grid.snapCoordinate(point.getX()), this.grid.snapCoordinate(point.getY()));
	}

	public void grantFocus() {
		this.hasFocus = true;
		this.repaint();
	}

	public void removeFocus() {
		this.hasFocus = false;
		this.repaint();
	}
}
