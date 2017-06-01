package org.workcraft.plugins.policy.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.util.GUI;

public class PolicySelectionTool extends SelectionTool {

    public PolicySelectionTool() {
        super(true, false, true, true);
    }

    @Override
    public void updateToolbar(JToolBar toolbar, final GraphEditor editor) {
        super.updateToolbar(toolbar, editor);

        JButton bundleButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/policy-selection-bundle.svg"), "Bundle selected transitions (" + DesktopApi.getMenuKeyMaskName() + "+B)");
        bundleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionBundle(editor);
            }
        });
        toolbar.add(bundleButton);

        JButton unbundleButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/policy-selection-unbundle.svg"), "Unbundle selected transitions (" + DesktopApi.getMenuKeyMaskName() + "+Shift+B)");
        unbundleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionUnbundle(editor);
            }
        });
        toolbar.add(unbundleButton);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getEditor().getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = (VisualNode) HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualPlace) {
                VisualPlace place = (VisualPlace) node;
                if (place.getReferencedPlace().getTokens() <= 1) {
                    e.getEditor().getWorkspaceEntry().saveMemento();

                    if (place.getReferencedPlace().getTokens() == 1) {
                        place.getReferencedPlace().setTokens(0);
                    } else {
                        place.getReferencedPlace().setTokens(1);
                    }
                }
                processed = true;
            }
        }

        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if (e.isMenuKeyDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_B:
                if (e.isShiftKeyDown()) {
                    selectionUnbundle(e.getEditor());
                } else {
                    selectionBundle(e.getEditor());
                }
                e.getEditor().repaint();
                return true;
            }
        }
        return super.keyPressed(e);
    }

    protected Collection<VisualBundledTransition> getSelectedTransitions(final GraphEditor editor) {
        Set<VisualBundledTransition> transitions = new HashSet<>();
        VisualPolicyNet visualModel = (VisualPolicyNet) editor.getModel();
        for (Node node : visualModel.getSelection()) {
            if (node instanceof VisualBundledTransition) {
                transitions.add((VisualBundledTransition) node);
            }
        }
        return transitions;
    }

    protected void selectionBundle(final GraphEditor editor) {
        Collection<VisualBundledTransition> transitions = getSelectedTransitions(editor);
        if (!transitions.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            VisualPolicyNet visualModel = (VisualPolicyNet) editor.getModel();
            visualModel.bundleTransitions(transitions);
        }
    }

    protected void selectionUnbundle(final GraphEditor editor) {
        Collection<VisualBundledTransition> transitions = getSelectedTransitions(editor);
        if (!transitions.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            VisualPolicyNet visualModel = (VisualPolicyNet) editor.getModel();
            visualModel.unbundleTransitions(transitions);
        }
    }

}
