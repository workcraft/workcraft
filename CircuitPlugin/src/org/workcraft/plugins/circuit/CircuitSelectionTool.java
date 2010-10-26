package org.workcraft.plugins.circuit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.SelectionTool;

public class CircuitSelectionTool extends SelectionTool implements ActionListener {

	VisualNode selectedNode = null;


	/*
	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		boolean allowDrag = true;

		if(drag==DRAG_MOVE) {
			for (Node n: e.getModel().getSelection()) {
				if (n instanceof VisualContact) {
					Node a = ((VisualContact)n).getParent();
					Node b = e.getModel().getCurrentLevel();
					if	(a!=b) {
						allowDrag=false;
						break;
					}
				}
			}

			if (!allowDrag) return;
		}

		super.mouseMoved(e);


	}
	*/

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);

		VisualModel model = e.getEditor().getModel();

		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			JPopupMenu popup = createPopupMenu(node);
			if (popup!=null)
				popup.show(e.getSystemEvent().getComponent(), e.getSystemEvent().getX(), e.getSystemEvent().getY());
		}
	}

	private JPopupMenu createPopupMenu(VisualNode node) {
		JPopupMenu popup = new JPopupMenu();
		this.selectedNode = node;
		if (node instanceof VisualFunctionComponent) {
			popup.setFocusable(false);
			popup.add(new JLabel("Function Component"));
			popup.addSeparator();

			JMenuItem addFunction = new JMenuItem("Add function");
			addFunction.addActionListener(this);

			popup.add(addFunction);
			return popup;
		} else if (node instanceof VisualCircuitComponent) {

			popup.setFocusable(false);
			popup.add(new JLabel("Circuit Component"));
			popup.addSeparator();

			JMenuItem addInput = new JMenuItem("Add input");
			addInput.addActionListener(this);

			JMenuItem addOutput = new JMenuItem("Add output");
			addOutput.addActionListener(this);

			popup.add(addInput);
			popup.add(addOutput);
			return popup;
		}

		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (selectedNode instanceof VisualFunctionComponent) {
			VisualFunctionComponent comp = (VisualFunctionComponent)selectedNode;

			if (e.getActionCommand().equals("Add function")) {
				comp.addFunction("x", null, false);
			}

		} else if (selectedNode instanceof VisualCircuitComponent) {
			VisualCircuitComponent comp = (VisualCircuitComponent)selectedNode;

			if (e.getActionCommand().equals("Add input")) {
				comp.addInput("", null);
			}

			if (e.getActionCommand().equals("Add output")) {
				comp.addOutput("", null);
			}
		}

	}

}
