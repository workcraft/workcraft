package org.workcraft.plugins.policy.tools;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.tools.PetriNetSelectionTool;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.WorkspaceEntry;

public class PolicyNetSelectionTool extends PetriNetSelectionTool {

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		super.keyPressed(e);
		WorkspaceEntry we = e.getEditor().getWorkspaceEntry();

		if (e.isCtrlDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				Set<BundledTransition> transitions = new HashSet<BundledTransition>();
				VisualPolicyNet visualModel = (VisualPolicyNet)editor.getModel();
				for (Node node : visualModel.getSelection()) {
					if (node instanceof VisualBundledTransition) {
						transitions.add(((VisualBundledTransition)node).getReferencedTransition());
					}
				}
				if (!transitions.isEmpty()) {
					we.saveMemento();
					if (e.isShiftDown()) {
						((PolicyNet)visualModel.getMathModel()).unbundle(transitions);
					} else {
						((PolicyNet)visualModel.getMathModel()).bundle(transitions);
					}
				}
				break;
			}
		}
		editor.repaint();
	}

}
