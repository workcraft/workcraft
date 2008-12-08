package org.workcraft.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.workcraft.dom.Model;
import org.workcraft.framework.Framework;
import org.workcraft.gui.edit.tools.ComponentCreationTool;
import org.workcraft.gui.edit.tools.ConnectionTool;
import org.workcraft.gui.edit.tools.GraphEditorTool;
import org.workcraft.gui.edit.tools.SelectionTool;

@SuppressWarnings("serial")
public class ToolboxView extends JPanel {
	Framework framework;

	SelectionTool selectionTool;
	ConnectionTool connectionTool;

	GraphEditorTool selectedTool;

	HashMap<JToggleButton, GraphEditorTool> map = new HashMap<JToggleButton, GraphEditorTool>();
	HashMap<GraphEditorTool, JToggleButton> reverseMap = new HashMap<GraphEditorTool, JToggleButton>();

	public void addTool (GraphEditorTool tool, boolean selected) {
		JToggleButton button = new JToggleButton();

		button.setSelected(selected);
		button.setFont(button.getFont().deriveFont(9.0f));
		button.setToolTipText(tool.getName());
		button.setText(tool.getName());

		button.setPreferredSize(new Dimension(120,20));
		button.setMinimumSize(new Dimension(120,20));
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

		this.add(button);
	}

	public void selectTool(GraphEditorTool tool) {
		if (selectedTool != null)
			reverseMap.get(selectedTool).setSelected(false);
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
		this.removeAll();
		this.setLayout(new FlowLayout (FlowLayout.LEFT, 5, 5));
		addCommonTools();

		for (Class<?> cls : model.getMathModel().getSupportedComponents()) {
			ComponentCreationTool tool = new ComponentCreationTool(cls);
			addTool(tool, false);
		}

		selectedTool = selectionTool;

		this.doLayout();
		this.repaint();
	}

	public GraphEditorTool getSelectedTool() {
		return selectedTool;
	}

	public void clearTools() {
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.add(new DisabledPanel(), BorderLayout.CENTER);
		this.repaint();
	}

	public ToolboxView(Framework framework) {
		super();
		this.framework = framework;

		selectionTool = new SelectionTool();
		connectionTool = new ConnectionTool();

		selectedTool = null;

		clearTools();
	}
}