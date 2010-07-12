/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;

import javax.swing.JPanel;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Overlay;
import org.workcraft.gui.PropertyEditorWindow;
import org.workcraft.gui.ToolboxWindow;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.Properties.Mix;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphEditorPanel extends JPanel implements StateObserver, GraphEditor {

	class Repainter extends StateSupervisor {
		@Override
		public void handleHierarchyEvent(HierarchyEvent e) {
			repaint();
		}

		@Override
		public void handleEvent(StateEvent e) {
			if (e instanceof PropertyChangedEvent || e instanceof TransformChangedEvent) {
				repaint();
				mainWindow.getPropertyView().repaint();
				if (!workspaceEntry.isChanged())
					workspaceEntry.setChanged(true);
			}
		}
	}

	class Resizer implements ComponentListener {

		@Override
		public void componentHidden(ComponentEvent e) {
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		}

		@Override
		public void componentResized(ComponentEvent e) {
			reshape();
			repaint();
		}

		@Override
		public void componentShown(ComponentEvent e) {
		}
	}

	public class GraphEditorFocusListener implements FocusListener {
		@Override
		public void focusGained(FocusEvent e) {
			repaint();
		}

		@Override
		public void focusLost(FocusEvent e) {
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

	private Overlay overlay = new Overlay();

	private boolean firstPaint = true;

	public GraphEditorPanel(MainWindow mainWindow, WorkspaceEntry workspaceEntry) {
		super (new BorderLayout());
		this.mainWindow = mainWindow;
		this.workspaceEntry = workspaceEntry;
		visualModel = (VisualModel) workspaceEntry.getObject();
		toolboxWindow = mainWindow.getToolboxWindow();

		new Repainter().attach(visualModel.getRoot()); //FIXME detach
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
		addFocusListener(new GraphEditorFocusListener());
		addComponentListener(new Resizer());

		addKeyListener(keyListener);

		add(overlay, BorderLayout.CENTER);
	}

	private void reshape() {
		view.setShape(15, 15, getWidth()-15, getHeight()-15);
		ruler.setShape(0, 0, getWidth(), getHeight());
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
			reshape();
			firstPaint = false;
		}

		g2d.transform(view.getTransform());

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

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

		paintChildren(g2d);
	}

	public VisualModel getModel() {
		return visualModel;
	}

	public Viewport getViewport() {
		return view;
	}

	public Point2D snap(Point2D point) {
		return new Point2D.Double(grid.snapCoordinate(point.getX()), grid.snapCoordinate(point.getY()));
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return workspaceEntry;
	}

	public MainWindow getMainWindow() {
		return mainWindow;
	}

	public void notify(HierarchyEvent e) {
		repaint();
		workspaceEntry.setChanged(true);
	}

	private void updatePropertyView() {
		final PropertyEditorWindow propertyWindow = mainWindow.getPropertyView();

		Collection<Node> selection = visualModel.getSelection();

		if (selection.size() == 1) {
			Node selected = selection.iterator().next();

			Mix mix = new Mix();

			Properties visualModelProperties = visualModel.getProperties(selected);

			mix.add(visualModelProperties);

			if (selected instanceof Properties)
				mix.add((Properties)selected);

			if (selected instanceof DependentNode) {
				for (Node n : ((DependentNode)selected).getMathReferences()) {
					mix.add(visualModel.getMathModel().getProperties(n));
					if (n instanceof Properties)
						mix.add((Properties)n);
				}
			}

			if(mix.isEmpty())
				propertyWindow.clearObject();
			else
				propertyWindow.setObject(mix);
		}
		else propertyWindow.clearObject();
	}

	public void notify(StateEvent e) {
		if (e instanceof SelectionChangedEvent) {
			updatePropertyView();
			repaint();
		}
	}

	@Override
	public EditorOverlay getOverlay() {
		return overlay;
	}
}