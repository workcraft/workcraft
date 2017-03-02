package org.workcraft.plugins.wtg.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.dtd.tools.DtdSelectionTool;
import org.workcraft.plugins.wtg.VisualState;
import org.workcraft.plugins.wtg.VisualWaveform;
import org.workcraft.plugins.wtg.VisualWtg;
import org.workcraft.workspace.WorkspaceEntry;

public class WtgSelectionTool extends DtdSelectionTool {

    @Override
    public JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
        JPopupMenu popup = super.createPopupMenu(node, editor);
        if (node instanceof VisualWaveform) {
            final VisualWaveform waveform = (VisualWaveform) node;
            if (popup != null) {
                popup.addSeparator();
            } else {
                popup = new JPopupMenu();
                popup.setFocusable(false);
            }
            JMenuItem centerPivotPointMenuItem = new JMenuItem("Center pivot point");
            centerPivotPointMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.getWorkspaceEntry().saveMemento();
                    waveform.centerPivotPoint(false, true);
                }
            });
            popup.add(centerPivotPointMenuItem);
        }
        return popup;
    }

    @Override
    public VisualNode hitTestPopup(VisualModel model, Point2D position) {
        return (VisualNode) HitMan.hitTestCurrentLevelFirst(position, model);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        VisualWtg model = (VisualWtg) e.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            Node node = HitMan.hitTestCurrentLevelFirst(e.getPosition(), model);
            if (node instanceof VisualState) {
                we.saveMemento();
                VisualState state = (VisualState) node;
                boolean isInitial = state.getReferencedState().isInitial();
                state.getReferencedState().setInitial(!isInitial);
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (model.getCurrentLevel() == model.getRoot()) {
            return "Double-click on a waveform to edit it. Double-click on a nodal state to toggle its marking.";
        }
        return super.getHintText(editor);
    }

}
