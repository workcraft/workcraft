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
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.GUI;

public class SelectionTool extends PetriNetSelectionTool {

    public SelectionTool() {
        super();
    }

    public SelectionTool(boolean enablePages) {
        super(false);
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        JPanel bundlePanel = new JPanel();
        controlPanel.add(bundlePanel);
        JButton bundleButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/icons/svg/selection-bundle.svg"), "Bundle selected transitions (Ctrl+B)");
        bundleButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionBundle(editor);
            }
        });
        bundlePanel.add(bundleButton);
        JButton unbundleButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/icons/svg/selection-unbundle.svg"), "Unbundle selected transitions (Ctrl+Shift+B)");
        unbundleButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionUnbundle(editor);
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
                    selectionUnbundle(e.getEditor());
                } else {
                    selectionBundle(e.getEditor());
                }
                break;
            }
        }
        e.getEditor().repaint();
    }

    protected Collection<VisualBundledTransition> getSelectedTransitions(final GraphEditor editor) {
        Set<VisualBundledTransition> transitions = new HashSet<VisualBundledTransition>();
        VisualPolicyNet visualModel = (VisualPolicyNet)editor.getModel();
        for (Node node : visualModel.getSelection()) {
            if (node instanceof VisualBundledTransition) {
                transitions.add((VisualBundledTransition)node);
            }
        }
        return transitions;
    }

    protected void selectionBundle(final GraphEditor editor) {
        Collection<VisualBundledTransition> transitions = getSelectedTransitions(editor);
        if (!transitions.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            VisualPolicyNet visualModel = (VisualPolicyNet)editor.getModel();
            visualModel.bundleTransitions(transitions);
        }
    }

    protected void selectionUnbundle(final GraphEditor editor) {
        Collection<VisualBundledTransition> transitions = getSelectedTransitions(editor);
        if (!transitions.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            VisualPolicyNet visualModel = (VisualPolicyNet)editor.getModel();
            visualModel.unbundleTransitions(transitions);
        }
    }

}
