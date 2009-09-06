package org.workcraft.gui.edit.graph;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import javax.swing.JPanel;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.StateEvent;
import org.workcraft.framework.observation.StateObserver;
import org.workcraft.framework.observation.StateSupervisor;
import org.workcraft.framework.workspace.WorkspaceEntry;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxWindow;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.PropertyEditable;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class GraphEditorPanel extends JPanel implements StateObserver, GraphEditor {

	class Repainter extends StateSupervisor {
		@Override
		public void handleHierarchyEvent(HierarchyEvent e) {
			System.out.println ("Repainting " + e + " " + e.getSender());
			repaint();
		}

		@Override
		public void handleEvent(StateEvent e) {
			System.out.println ("Repainting " + e + " " + e.getSender());
			repaint();
		}
	}

	private static final long serialVersionUID = 1L;

	protected VisualModel visualModel;
	protected WorkspaceEntry workspaceEntry;

	protected MainWindow mainWindow;
	protected ToolboxWindow toolboxWindow;

	protected Viewport view;
	protected Grid grid;
	protected Ruler ruler;

	protected Stroke borderStroke = new BasicStroke(2);

	private boolean firstPaint = true;

	public GraphEditorPanel(MainWindow mainWindow, WorkspaceEntry workspaceEntry) {
		this.mainWindow = mainWindow;
		this.workspaceEntry = workspaceEntry;
		visualModel = (VisualModel) workspaceEntry.getModel();
		toolboxWindow = mainWindow.getToolboxWindow();

		new Repainter().attach(visualModel.getRoot());
		visualModel.addObserver(this);

		view = new Viewport(0, 0, getWidth(), getHeight());
		grid = new Grid();

		ruler = new Ruler();
		view.addListener(grid);
		grid.addListener(ruler);

		GraphEditorPanelMouseListener mouseListener = new GraphEditorPanelMouseListener(this, toolboxWindow);
		GraphEditorPanelKeyListener keyListener = new GraphEditorPanelKeyListener(this, toolboxWindow);

		addMouseMotionListener(mouseListener);
		addMouseListener(mouseListener);
		addMouseWheelListener(mouseListener);
		addFocusListener(new GraphEditorFocusListener(this));

		addKeyListener(keyListener);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;

		AffineTransform screenTransform = (AffineTransform)g2d.getTransform().clone();

		g2d.setBackground(CommonVisualSettings.getBackgroundColor());
		g2d.clearRect(0, 0, getWidth(), getHeight());

		grid.draw(g2d);
		g2d.setTransform(screenTransform);

		if (firstPaint) {
			componentResized(null);
			firstPaint = false;
		}

		g2d.transform(view.getTransform());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		visualModel.draw(g2d);

		if (hasFocus())
			toolboxWindow.getTool().drawInUserSpace(this, g2d);

		g2d.setTransform(screenTransform);

		ruler.draw(g2d);

		if (hasFocus()) {
			toolboxWindow.getTool().drawInScreenSpace(this, g2d);
			g2d.setTransform(screenTransform);

			g2d.setStroke(borderStroke);
			g2d.setColor(CommonVisualSettings.getForegroundColor());
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

	public void focusGained() {
		this.repaint();
	}

	public void focusLost() {
		this.repaint();
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}

	public void onSelectionChanged(Collection<Node> selection) {
		repaint();
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public void notify(HierarchyEvent e) {
		repaint();
		workspaceEntry.setUnsaved(true);
	}

	public void notify(StateEvent e) {
		Collection<Node> selection = visualModel.getSelection();
		if (selection.size() == 1 && selection.iterator().next() instanceof PropertyEditable) {
			mainWindow.getPropertyView().setObject((PropertyEditable)selection.iterator().next());
		} else {
			mainWindow.getPropertyView().clearObject();
		}

		repaint();
		workspaceEntry.setUnsaved(true);
	}
}
