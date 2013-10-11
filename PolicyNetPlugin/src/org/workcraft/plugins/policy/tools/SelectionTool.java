package org.workcraft.plugins.policy.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.workcraft.dom.Node;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.tools.PetriNetSelectionTool;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.GUI;

public class SelectionTool extends PetriNetSelectionTool {

	public SelectionTool() {
		super();
	}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		createInterface();
	}

	private void createInterface() {
		JPanel bundlePanel = new JPanel();
		controlPanel.add(bundlePanel);
		JButton bundleButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-bundle.svg"), "Bundle selected transitions (Ctrl+B)");
		bundleButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionBundle();
			}
		});
		bundlePanel.add(bundleButton);
		JButton unbundleButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-unbundle.svg"), "Unbundle selected transitions (Ctrl+Shift+B)");
		unbundleButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionUnbundle();
			}
		});
		bundlePanel.add(unbundleButton);
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e) {
		super.keyPressed(e);

		if (e.isCtrlDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				if (e.isShiftDown()) {
					selectionUnbundle();
				} else {
					selectionBundle();
				}
				break;
			}
		}
		getEditor().repaint();
	}

	protected Collection<BundledTransition> getSelectedTransitions() {
		Set<BundledTransition> transitions = new HashSet<BundledTransition>();
		VisualPolicyNet visualModel = (VisualPolicyNet)getEditor().getModel();
		for (Node node : visualModel.getSelection()) {
			if (node instanceof VisualBundledTransition) {
				transitions.add(((VisualBundledTransition)node).getReferencedTransition());
			}
		}
		return transitions;
	}

	protected void selectionBundle() {
		Collection<BundledTransition> transitions = getSelectedTransitions();
		if (!transitions.isEmpty()) {
			PolicyNet model = (PolicyNet)getEditor().getModel().getMathModel();
			getEditor().getWorkspaceEntry().saveMemento();
			model.bundleTransitions(transitions);
		}
	}

	protected void selectionUnbundle() {
		Collection<BundledTransition> transitions = getSelectedTransitions();
		if (!transitions.isEmpty()) {
			PolicyNet model = (PolicyNet)getEditor().getModel().getMathModel();
			getEditor().getWorkspaceEntry().saveMemento();
			model.unbundleTransitions(transitions);
		}
	}

}
