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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
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

	public static final int TOOL_ICON_CROP_SIZE = 16;

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
		button.setMargin(new Insets(2,2,2,2));

		Insets insets = button.getInsets();
		int minSize = TOOL_ICON_CROP_SIZE+Math.max(insets.left+insets.right, insets.top+insets.bottom);

		Icon icon = tool.getIcon();
		if(icon==null) {
			button.setText(tool.getLabel());
			button.setPreferredSize(new Dimension(120,minSize));
		}
		else {
			BufferedImage crop = new BufferedImage(TOOL_ICON_CROP_SIZE, TOOL_ICON_CROP_SIZE,
					BufferedImage.TYPE_INT_ARGB);
			icon.paintIcon(button, crop.getGraphics(), 8-icon.getIconWidth()/2, 8-icon.getIconHeight()/2);
			button.setIcon(new ImageIcon(crop));
			button.setPreferredSize(new Dimension(minSize,minSize));
		}


		int hotKeyCode = tool.getHotKeyCode();
		if ( hotKeyCode != -1)
			button.setToolTipText("["+Character.toString((char)hotKeyCode)+"] " + tool.getLabel());
		else
			button.setToolTipText(tool.getLabel());

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
		MainWindow mainWindow = framework.getMainWindow();

		if (selectedTool != null) {
			tracker = trackerMap.get(selectedTool);

			selectedTool.deactivated(mainWindow.getCurrentEditor());
			((VisualGroup)mainWindow.getCurrentEditor().getModel().getRoot()).clearColorisation();
			reverseMap.get(selectedTool).setSelected(false);
		}

		if (tracker == null)
			tracker = trackerMap.get(tool);

		if (tracker != null)
			tracker.track(tool);

		tool.activated(mainWindow.getCurrentEditor());
		mainWindow.getToolInterfaceWindow().setTool(tool);
		reverseMap.get(tool).setSelected(true);
		selectedTool = tool;
		mainWindow.repaintCurrentEditor();
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

		setLayout(new SimpleFlowLayout (5, 5));

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