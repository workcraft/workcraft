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
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;

public class CircuitSelectionTool extends SelectionTool {

	VisualNode selectedNode = null;

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
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
			final VisualFunctionComponent component = (VisualFunctionComponent)node;

			popup.setFocusable(false);
			popup.add(new JLabel("Function component"));
			popup.addSeparator();
			{
				JMenuItem addOutput = new JMenuItem("Add output (EAST)");
				addOutput.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						VisualCircuit vcircuit = (VisualCircuit)editor.getModel();
						vcircuit.getOrCreateContact(component, null, IOType.OUTPUT);
						component.setContactsDefaultPosition();
					}
				});
				popup.add(addOutput);
			}
			{
				JMenuItem addInput = new JMenuItem("Add input (WEST)");
				addInput.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						VisualCircuit vcircuit = (VisualCircuit)editor.getModel();
						vcircuit.getOrCreateContact(component, null, IOType.INPUT);
						component.setContactsDefaultPosition();
					}
				});
				popup.add(addInput);
			}
			popup.addSeparator();
			{
				JMenuItem defaultContactPosition = new JMenuItem("Set contacts in default position");
				defaultContactPosition.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						component.setContactsDefaultPosition();
					}
				});
				popup.add(defaultContactPosition);
			}
			{
				JMenuItem centerPivotPoint = new JMenuItem("Center pivot point");
				centerPivotPoint.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						editor.getWorkspaceEntry().saveMemento();
						component.centerPivotPoint();
					}
				});
				popup.add(centerPivotPoint);
			}
			return popup;
		}

		return null;
	}

}
