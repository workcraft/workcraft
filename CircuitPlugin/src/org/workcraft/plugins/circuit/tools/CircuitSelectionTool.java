package org.workcraft.plugins.circuit.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualFunctionComponent;

public class CircuitSelectionTool extends SelectionTool {

	VisualNode selectedNode = null;

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		boolean processed = false;
		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			VisualModel model = e.getEditor().getModel();
			Node node = HitMan.hitTestForSelection(e.getPosition(), model);
			JPopupMenu popup = createPopupMenu(node, e.getEditor());
			if (popup != null) {
				popup.show(e.getSystemEvent().getComponent(),
						e.getSystemEvent().getX(), e.getSystemEvent().getY());
			}
			processed = true;
		}
		if (!processed) {
			super.mouseClicked(e);
		}
	}

	private JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
		JPopupMenu popup = new JPopupMenu();

		if (node instanceof VisualFunctionComponent) {
			final VisualFunctionComponent comp = (VisualFunctionComponent) node;

			popup.setFocusable(false);
			popup.add(new JLabel("Function Component"));
			popup.addSeparator();

			JMenuItem addFunction = new JMenuItem("Add function");
			addFunction.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editor.getWorkspaceEntry().saveMemento();
					comp.addFunction("x", null, false);
				}
			});

			popup.add(addFunction);
			return popup;
		}

		if (node instanceof VisualCircuitComponent) {
			final VisualCircuitComponent comp = (VisualCircuitComponent) node;

			popup.setFocusable(false);
			popup.add(new JLabel("Circuit Component"));
			popup.addSeparator();

			JMenuItem addInput = new JMenuItem("Add input");
			addInput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editor.getWorkspaceEntry().saveMemento();
					comp.addInput("", null);
				}
			});

			JMenuItem addOutput = new JMenuItem("Add output");
			addOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editor.getWorkspaceEntry().saveMemento();
					comp.addOutput("", null);
				}
			});

			popup.add(addInput);
			popup.add(addOutput);
			return popup;
		}

		return null;
	}

}
