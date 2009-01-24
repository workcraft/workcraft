package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelListener;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditable;

public class GraphEditorPanel extends JPanel implements ComponentListener, VisualModelListener, PropertyChangeListener, GraphEditor {
	private static final long serialVersionUID = 1L;

	protected VisualModel visualModel;
	protected WorkspaceEntry workspaceEntry;

	protected MainWindow mainWindow;
	protected ToolboxWindow toolboxWindow;

	protected Viewport view;
	protected Grid grid;
	protected Ruler ruler;

	protected boolean hasFocus = false;

	protected Color background = Color.WHITE;
	protected Color focusBorderColor = Color.BLACK;
	protected Stroke borderStroke = new BasicStroke(2);

	public GraphEditorPanel(MainWindow mainWindow, WorkspaceEntry workspaceEntry) {
		this.mainWindow = mainWindow;
		this.workspaceEntry = workspaceEntry;
		visualModel = workspaceEntry.getModel().getVisualModel();
		toolboxWindow = mainWindow.getToolboxWindow();

		visualModel.addListener(this);

		view = new Viewport(0, 0, getWidth(), getHeight());
		grid = new Grid();

		ruler = new Ruler();
		view.addListener(grid);
		grid.addListener(ruler);
		addComponentListener(this);

		GraphEditorPanelMouseListener mouseListener = new GraphEditorPanelMouseListener(this, toolboxWindow);
		GraphEditorPanelKeyListener keyListener = new GraphEditorPanelKeyListener(this, toolboxWindow);

		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseListener);

		addKeyListener(keyListener);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform screenTransform = g2d.getTransform();

		g2d.setBackground(background);
		g2d.clearRect(0, 0, getWidth(), getHeight());

		grid.draw(g2d);
		g2d.setTransform(screenTransform);

		g2d.transform(view.getTransform());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		visualModel.draw(g2d);

		if (hasFocus)
			toolboxWindow.getTool().drawInUserSpace(this, g2d);

		g2d.setTransform(screenTransform);

		ruler.draw(g2d);

		if (hasFocus) {
			toolboxWindow.getTool().drawInScreenSpace(this, g2d);
			g2d.setTransform(screenTransform);

			g2d.setStroke(borderStroke);
			g2d.setColor(focusBorderColor);
			g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);
		}


	}


	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}



	public void componentResized(ComponentEvent e) {
		view.setShape(15, 15, getWidth()-15, getHeight()-15);
		ruler.setShape(0, 0, getWidth(), getHeight());
		repaint();
	}

	public void componentShown(ComponentEvent e) {
	}

	public VisualModel getModel() {
		return visualModel;
	}

	public Viewport getViewport() {
		return view;
	}

	public void snap(Point2D point) {
		point.setLocation(grid.snapCoordinate(point.getX()), grid.snapCoordinate(point.getY()));
	}

	public boolean hasFocus() {
		return hasFocus;
	}

	public void grantFocus() {
		hasFocus = true;
		this.repaint();
	}

	public void removeFocus() {
		hasFocus = false;
		this.repaint();
	}


	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}

	public void visualNodePropertyChanged(VisualNode n) {
		repaint();
		mainWindow.getPropertyView().repaint();
	}

	public void componentPropertyChanged(Component c) {
		repaint();
		mainWindow.getPropertyView().repaint();
	}

	public void connectionPropertyChanged(Connection c) {
		repaint();
		mainWindow.getPropertyView().repaint();
	}

	public void modelStructureChanged() {
		repaint();
		mainWindow.getPropertyView().repaint();
	}

	public void layoutChanged() {
		repaint();
		mainWindow.getPropertyView().repaint();
	}

	public void selectionChanged() {
		repaint();

		VisualNode selection[] = visualModel.getSelection();
		if (selection.length == 1 && selection[0] instanceof PropertyEditable) {

			PropertyEditable p = mainWindow.getPropertyView().getObject();

			if (p!=null)
				p.removeListener(this);

			((PropertyEditable)selection[0]).addListener(this);
			mainWindow.getPropertyView().setObject((PropertyEditable)selection[0]);

		} else {
			mainWindow.getPropertyView().clearObject();
		}
	}

	public void propertyChanged(String propertyName, Object sender) {
		visualModel.fireLayoutChanged();
	}
}
