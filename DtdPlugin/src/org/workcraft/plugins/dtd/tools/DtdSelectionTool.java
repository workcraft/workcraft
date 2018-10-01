package org.workcraft.plugins.dtd.tools;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.dtd.*;
import org.workcraft.workspace.WorkspaceEntry;

public class DtdSelectionTool extends SelectionTool {

    private boolean draggingExitEvent = false;

    public DtdSelectionTool() {
        super(false, false, false, false);
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(true);
        we.setCanCopy(false);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        VisualDtd model = (VisualDtd) e.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualSignal) {
                we.saveMemento();
                VisualSignal signal = (VisualSignal) node;
                TransitionEvent.Direction direction = getDesiredDirection(e);
                model.appendSignalEvent(signal, direction);
                processed = true;
            } else if ((node instanceof VisualLevelConnection) && (e.getClickCount() > 1)) {
                we.saveMemento();
                VisualLevelConnection connection = (VisualLevelConnection) node;
                model.insertSignalPulse(connection);
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    private TransitionEvent.Direction getDesiredDirection(GraphEditorMouseEvent e) {
        switch (e.getKeyModifiers()) {
        case MouseEvent.SHIFT_DOWN_MASK:
            return TransitionEvent.Direction.RISE;
        case MouseEvent.CTRL_DOWN_MASK:
            return TransitionEvent.Direction.FALL;
        case MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK:
            return TransitionEvent.Direction.DESTABILISE;
        default:
            return null;
        }
    }

    @Override
    public void beforeSelectionModification(final GraphEditor editor) {
        super.beforeSelectionModification(editor);
        VisualModel model = editor.getModel();
        ArrayList<Node> selection = new ArrayList<>(model.getSelection());
        for (Node node : selection) {
            VisualConnection connection = null;
            if (node instanceof VisualConnection) {
                connection = (VisualConnection) node;
            } else if (node instanceof ControlPoint) {
                connection = ConnectionHelper.getParentConnection((ControlPoint) node);
            }
            if (connection instanceof VisualLevelConnection) {
                model.removeFromSelection(node);
            }
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Double-click on a signal to add its transition. Hold Shift for rising edge, " +
                DesktopApi.getMenuKeyMaskName() + " for falling edge, or both keys for unstable state.";
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        Container container = model.getCurrentLevel();
        if (e.getButtonModifiers() == MouseEvent.BUTTON1_DOWN_MASK) {
            Point2D startPos = e.getStartPosition();
            Node hitNode = HitMan.hitFirstInCurrentLevel(startPos, model);
            if ((e.getKeyModifiers() == 0) && (hitNode instanceof VisualExitEvent) && (model instanceof VisualDtd)) {
                VisualDtd visualDtd = (VisualDtd) model;
                VisualTransformableNode visualNode = (VisualTransformableNode) container;
                Collection<Node> selection = new HashSet<>();
                selection.addAll(visualDtd.getSelection());
                for (VisualTransformableNode component : visualNode.getComponents()) {
                    if (component instanceof VisualSignal) {
                        VisualExitEvent visualExit = ((VisualSignal) component).getVisualSignalExit();
                        if (!selection.contains(visualExit)) {
                            selection.add(visualExit);
                        }
                    }
                }
                visualDtd.select(selection);
                super.startDrag(e);
                draggingExitEvent = true;
                visualDtd.alignExitEventsToEvent((VisualExitEvent) hitNode);
                return;
            }
        }
        super.startDrag(e);
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        super.mouseMoved(e);
        if (draggingExitEvent) {
            GraphEditor editor = e.getEditor();
            VisualModel model = editor.getModel();
            Container container = model.getCurrentLevel();
            if (model instanceof VisualDtd && container instanceof VisualTransformableNode) {
                VisualDtd visualDtd = (VisualDtd) model;
                visualDtd.alignExitEventsToRightmostEvent();
            }
        }
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
        draggingExitEvent = false;
        super.finishDrag(e);
    }

}
