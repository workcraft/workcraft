package org.workcraft.plugins.policy.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.policy.VisualBundledTransition;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PolicySelectionTool extends SelectionTool {

    private static final String BUNDLE_ICON = "images/policy-selection-bundle.svg";
    private static final String UNBUNDLE_ICON = "images/policy-selection-unbundle.svg";

    private static final String BUNDLE_HINT = "Bundle selected transitions (" + DesktopApi.getMenuKeyName() + "-B)";
    private static final String UNBUNDLE_HINT = "Unbundle selected transitions (" + DesktopApi.getMenuKeyName() + "+Shift-B)";

    public PolicySelectionTool() {
        super(true, false, true, true);
    }

    @Override
    public void updateControlsToolbar(JToolBar toolbar, final GraphEditor editor) {
        super.updateControlsToolbar(toolbar, editor);
        JButton bundleButton = GuiUtils.createIconButton(BUNDLE_ICON, BUNDLE_HINT, event -> selectionBundle(editor));
        JButton unbundleButton = GuiUtils.createIconButton(UNBUNDLE_ICON, UNBUNDLE_HINT, event -> selectionUnbundle(editor));
        toolbar.add(bundleButton);
        toolbar.add(unbundleButton);
        toolbar.addSeparator();
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getEditor().getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualPlace) {
                if (e.isMenuKeyDown()) {
                    VisualPlace place = (VisualPlace) node;
                    if (place.getReferencedComponent().getTokens() <= 1) {
                        e.getEditor().getWorkspaceEntry().saveMemento();

                        if (place.getReferencedComponent().getTokens() == 1) {
                            place.getReferencedComponent().setTokens(0);
                        } else {
                            place.getReferencedComponent().setTokens(1);
                        }
                    }
                    processed = true;
                }
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
        VisualPolicy visualModel = (VisualPolicy) editor.getModel();
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
            VisualPolicy visualModel = (VisualPolicy) editor.getModel();
            visualModel.bundleTransitions(transitions);
        }
    }

    protected void selectionUnbundle(final GraphEditor editor) {
        Collection<VisualBundledTransition> transitions = getSelectedTransitions(editor);
        if (!transitions.isEmpty()) {
            editor.getWorkspaceEntry().saveMemento();
            VisualPolicy visualModel = (VisualPolicy) editor.getModel();
            visualModel.unbundleTransitions(transitions);
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        if (getDragState() == DragState.NONE) {
            if (getCurrentNode() instanceof VisualPlace) {
                return DesktopApi.getMenuKeyName() + "+double-click to toggle place marking.";
            }
        }
        return super.getHintText(editor);
    }

}
