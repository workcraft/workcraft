package org.workcraft.plugins.dtd.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.event.MouseEvent;

public class DtdSelectionTool extends SelectionTool {

    private VisualSignal swappingSignal = null;

    public DtdSelectionTool() {
        super(false, false, false, false);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        VisualDtd dtd = (VisualDtd) e.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), dtd);
            if (node instanceof VisualSignal signal) {
                Signal.State state = getLastState(dtd, signal);
                TransitionEvent.Direction direction = getDesiredDirection(state, e.getKeyModifiers());
                if (direction != null) {
                    we.saveMemento();
                    dtd.appendSignalEvent(signal, direction);
                }
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (e.getButtonModifiers() == MouseEvent.BUTTON1_DOWN_MASK) {
            Node hitNode = HitMan.hitFirstInCurrentLevel(e.getStartPosition(), model);
            if ((swappingSignal == null) && e.isAltKeyDown()
                    && (hitNode instanceof VisualSignal) && (model instanceof VisualDtd)) {

                swappingSignal = (VisualSignal) hitNode;
            }
        }
        super.startDrag(e);
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        super.mouseMoved(e);
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (swappingSignal != null) {
            if (e.isAltKeyDown() && (e.getButtonModifiers() == MouseEvent.BUTTON1_DOWN_MASK)) {
                Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
                if (node instanceof VisualSignal visualSignal) {
                    double y = visualSignal.getY();
                    visualSignal.setY(swappingSignal.getY());
                    swappingSignal.setY(y);
                }
            } else {
                swappingSignal = null;
            }
        }
    }

    private Signal.State getLastState(VisualDtd dtd, VisualSignal signal) {
        VisualExitEvent exit = signal.getVisualSignalExit();
        VisualEvent event = DtdUtils.getPrevVisualEvent(dtd, exit);
        return DtdUtils.getNextState(event.getReferencedComponent());
    }

    private TransitionEvent.Direction getDesiredDirection(Signal.State state, int mask) {
        if (state == Signal.State.UNSTABLE) {
            return switch (mask) {
                case MouseEvent.SHIFT_DOWN_MASK -> TransitionEvent.Direction.RISE;
                case MouseEvent.CTRL_DOWN_MASK -> TransitionEvent.Direction.FALL;
                default -> TransitionEvent.Direction.STABILISE;
            };
        }
        if (state == Signal.State.HIGH) {
            if (mask == (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK)) {
                return TransitionEvent.Direction.DESTABILISE;
            }
            return TransitionEvent.Direction.FALL;
        }
        if (state == Signal.State.LOW) {
            if (mask == (MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK)) {
                return TransitionEvent.Direction.DESTABILISE;
            }
            return TransitionEvent.Direction.RISE;
        }
        if (state == Signal.State.STABLE) {
            return TransitionEvent.Direction.DESTABILISE;
        }
        return null;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Double-click on signal to add an event. In unstable state hold Shift to rise, " +
                DesktopApi.getMenuKeyName() + " to fall; in high/low state hold Shift+" +
                DesktopApi.getMenuKeyName() + " to destabilise. Hold Alt/AltGr to swap signals.";
    }

}
