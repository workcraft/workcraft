package org.workcraft.gui.edit.graph;

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

import org.workcraft.dom.Component;
import org.workcraft.dom.Model;
import org.workcraft.framework.Framework;
import org.workcraft.gui.DisabledPanel;
import org.workcraft.gui.edit.tools.ComponentCreationTool;
import org.workcraft.gui.edit.tools.ConnectionTool;
import org.workcraft.gui.edit.tools.GraphEditorKeyListener;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.SelectionTool;
import org.workcraft.gui.edit.tools.ToolProvider;
import org.workcraft.gui.events.GraphEditorKeyEvent;

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
		button.setToolTipText(tool.getName());


		int hotKeyCode = tool.getHotKeyCode();
		if ( hotKeyCode != -1)
			button.setText("["+Character.toString((char)hotKeyCode)+"] " + tool.getName());
		else
			button.setText(tool.getName());

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
			framework.getMainWindow().getCurrentEditor().getModel().getVisualModel().clearColorisation();
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

	public void setToolsForModel (Model model) {
		map.clear();
		reverseMap.clear();
		clearTrackers();
		removeAll();
		selectedTool = null;

		setLayout(new FlowLayout (FlowLayout.LEFT, 5, 5));

		addCommonTools();

		for (Class<? extends Component> cls : model.getMathModel().getSupportedComponents()) {
			ComponentCreationTool tool = new ComponentCreationTool(cls);
			addTool(tool, false);
		}

		for (GraphEditorTool tool : model.getVisualModel().getAdditionalTools())
			addTool(tool, false);

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