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

package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.workcraft.Framework;
import org.workcraft.annotations.Annotations;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.gui.graph.tools.ToolProvider;

@SuppressWarnings("serial")
public class ToolboxWindow extends JPanel implements ToolProvider, GraphEditorKeyListener {

	class ToolTracker {
		LinkedList<GraphEditorTool> tools = new LinkedList<GraphEditorTool>();
		ListIterator<GraphEditorTool> iter = tools.listIterator();

		public void addTool(GraphEditorTool tool) {
			tools.add(tool);
			iter = tools.listIterator();
		}

		public void reset() {
			iter = tools.listIterator();
		}

		public GraphEditorTool getNextTool() {
			GraphEditorTool ret = iter.next();
			if (iter.nextIndex() == tools.size())
				iter = tools.listIterator();
			return ret;
		}

		public void track(GraphEditorTool tool) {
			int index = tools.indexOf(tool);
			if (index == -1)
				iter = tools.listIterator(0);
			else {
				if (( tools.size()-1) == index )
					iter = tools.listIterator(0);
				else
					iter = tools.listIterator(index + 1);
			}
		}
	}

	Framework framework;

	SelectionTool selectionTool;
	ConnectionTool connectionTool;

	GraphEditorTool selectedTool;
	ToolTracker currentTracker = null;

	HashMap<JToggleButton, GraphEditorTool> map = new HashMap<JToggleButton, GraphEditorTool>();
	HashMap<GraphEditorTool, JToggleButton> reverseMap = new HashMap<GraphEditorTool, JToggleButton>();
	HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<Integer, ToolTracker>();
	HashMap<GraphEditorTool, ToolTracker> trackerMap = new HashMap<GraphEditorTool, ToolTracker>();

	public void addTool (GraphEditorTool tool, boolean selected) {
		JToggleButton button = new JToggleButton();

		button.setFocusable(false);
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setSelected(selected);
		button.setFont(button.getFont().deriveFont(9.0f));
		button.setToolTipText(tool.getLabel());


		int hotKeyCode = tool.getHotKeyCode();
		if ( hotKeyCode != -1)
			button.setText("["+Character.toString((char)hotKeyCode)+"] " + tool.getLabel());
		else
			button.setText(tool.getLabel());

		button.setPreferredSize(new Dimension(120,20));
		button.setMinimumSize(new Dimension(0,0));
		button.setMaximumSize(new Dimension(120,20));

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JToggleButton button = (JToggleButton)e.getSource();
				GraphEditorTool tool = map.get(button);
				selectTool(tool);

			}
		});

		map.put(button, tool);
		reverseMap.put(tool, button);

		if (hotKeyCode != -1) {
			ToolTracker tracker = hotkeyMap.get(hotKeyCode);
			if (tracker == null) {
				tracker = new ToolTracker();
				hotkeyMap.put(hotKeyCode, tracker);
			}
			tracker.addTool(tool);

			trackerMap.put(tool, tracker);
		}

		this.add(button);

		if (selected)
			selectTool(tool);
	}

	private void clearTrackers() {
		hotkeyMap.clear();
		trackerMap.clear();
	}

	public void selectTool(GraphEditorTool tool) {
		ToolTracker tracker = null;

		if (selectedTool != null) {
			tracker = trackerMap.get(selectedTool);

			selectedTool.deactivated(framework.getMainWindow().getCurrentEditor());
			((VisualGroup)framework.getMainWindow().getCurrentEditor().getModel().getRoot()).clearColorisation();
			reverseMap.get(selectedTool).setSelected(false);
		}

		if (tracker == null)
			tracker = trackerMap.get(tool);

		if (tracker != null)
			tracker.track(tool);

		tool.activated(framework.getMainWindow().getCurrentEditor());
		reverseMap.get(tool).setSelected(true);
		selectedTool = tool;
		framework.getMainWindow().repaintCurrentEditor();
	}

	public void addCommonTools() {
		addTool(selectionTool, true);
		addTool(connectionTool, false);
	}

	public void setToolsForModel (VisualModel model) {
		map.clear();
		reverseMap.clear();
		clearTrackers();
		removeAll();
		selectedTool = null;

		setLayout(new FlowLayout (FlowLayout.LEFT, 5, 5));

		addCommonTools();

		for (Class<?> cls : Annotations.getDefaultCreateButtons(model.getClass())) {
			NodeGeneratorTool tool = new NodeGeneratorTool(new DefaultNodeGenerator(cls));
			addTool(tool, false);
		}
		Class<? extends CustomToolsProvider> customTools = Annotations.getCustomToolsProvider(model.getClass());
		if(customTools != null)
		{
			CustomToolsProvider provider = null;
			try {
				provider = customTools.getConstructor().newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(provider != null)
				for(GraphEditorTool tool : provider.getTools())
					addTool(tool, false);
		}

		for (Class<? extends GraphEditorTool>  tool : Annotations.getCustomTools(model.getClass()))
			try {
				addTool( tool.newInstance() , false);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		doLayout();
		this.repaint();
	}

	public void clearTools() {
		removeAll();
		clearTrackers();
		setLayout(new BorderLayout());
		this.add(new DisabledPanel(), BorderLayout.CENTER);
		this.repaint();
	}

	public ToolboxWindow(Framework framework) {
		super();
		this.framework = framework;
		this.setFocusable(false);

		selectionTool = new SelectionTool();
		connectionTool = new ConnectionTool();
		selectedTool = null;

		clearTools();
	}

	public GraphEditorTool getTool() {
		return selectedTool;
	}

	public void keyPressed(GraphEditorKeyEvent event) {
		if (!event.isAltDown() && !event.isCtrlDown() && !event.isShiftDown()) {
			int keyCode = event.getKeyCode();
			ToolTracker tracker = hotkeyMap.get(keyCode);
			if (tracker != null)
				selectTool(tracker.getNextTool());
			else
				selectedTool.keyPressed(event);
		} else
			selectedTool.keyPressed(event);
	}

	public void keyReleased(GraphEditorKeyEvent event) {
		selectedTool.keyReleased(event);

	}

	public void keyTyped(GraphEditorKeyEvent event) {
		selectedTool.keyTyped(event);
	}
}