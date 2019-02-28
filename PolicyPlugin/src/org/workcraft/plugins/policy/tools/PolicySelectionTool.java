package org.workcraft.plugins.policy.tools;

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
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.utils.GuiUtils;

public class PolicySelectionTool extends SelectionTool {

    public PolicySelectionTool() {
        super(true, false, true, true);
    }

    @Override
    public void updateControlsToolbar(JToolBar toolbar, final GraphEditor editor) {
        super.updateControlsToolbar(toolbar, editor);

        JButton bundleButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/policy-selection-bundle.svg"),
                "Bundle selected transitions (" + DesktopApi.getMenuKeyName() + "-B)");
        bundleButton.addActionListener(event -> selectionBundle(editor));
        toolbar.add(bundleButton);

        JButton unbundleButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/policy-selection-unbundle.svg"),
                "Unbundle selected transitions (" + DesktopApi.getMenuKeyName() + "+Shift-B)");
        unbundleButton.addActionListener(event -> selectionUnbundle(editor));
        toolbar.add(unbundleButton);
        if (toolbar.getComponentCount() > 0) {
            toolbar.addSeparator();
        }
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
