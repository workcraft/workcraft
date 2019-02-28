package org.workcraft.gui.events;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.tools.GraphEditor;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class GraphEditorMouseEvent {
    GraphEditor editor;
    MouseEvent event;
    Point2D position;
    Point2D prevPosition;
    Point2D startPosition;

    public GraphEditorMouseEvent(GraphEditor editor, MouseEvent e) {
        this.editor = editor;
        event = e;
        if (editor != null) {
            position = editor.getViewport().screenToUser(e.getPoint());
        } else {
            position = new Point2D.Double(0, 0);
        }
    }

    public GraphEditorMouseEvent(GraphEditor editor, MouseEvent e, Point2D startPosition, Point2D prevPosition) {
        this(editor, e);
        this.startPosition = startPosition;
        this.prevPosition = prevPosition;
    }

    public GraphEditor getEditor() {
        return editor;
    }

    public VisualModel getModel() {
        return editor.getModel();
    }

    public MouseEvent getSystemEvent() {
        return event;
    }

    public int getID() {
        return event.getID();
    }

    public Point2D getPosition() {
        return position;
    }

    public Point2D getStartPosition() {
        return startPosition;
    }

    public Point2D getPrevPosition() {
        return prevPosition;
    }

    public int getButton() {
        return event.getButton();
    }

    public int getClickCount() {
        return event.getClickCount();
    }

    public int getModifiers() {
        return event.getModifiersEx();
    }

    public int getKeyModifiers() {
        return event.getModifiersEx() & (MouseEvent.SHIFT_DOWN_MASK | DesktopApi.getMenuKeyMouseMask() | MouseEvent.ALT_DOWN_MASK);
    }

    public int getButtonModifiers() {
        // BUTTON2 is ignored as it is reserved for panning
        return event.getModifiersEx() & (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK);
    }

    private boolean isMaskHit(int mask) {
        return (getModifiers() & mask) == mask;
    }

    public boolean isCtrlKeyDown() {
        return isMaskHit(MouseEvent.CTRL_DOWN_MASK);
    }

    public boolean isShiftKeyDown() {
        return isMaskHit(MouseEvent.SHIFT_DOWN_MASK);
    }

    public boolean isAltKeyDown() {
        return isMaskHit(MouseEvent.ALT_DOWN_MASK);
    }

    public boolean isAltGraphKeyDown() {
        return isMaskHit(MouseEvent.ALT_GRAPH_DOWN_MASK);
    }

    public boolean isMetaKeyDown() {
        return isMaskHit(MouseEvent.META_DOWN_MASK);
    }

    public boolean isMenuKeyDown() {
        if (DesktopApi.getMenuKeyMask() == ActionEvent.META_MASK) {
            return isMetaKeyDown();
        }
        return isCtrlKeyDown();
    }

    public boolean isExtendKeyDown() {
        return isAltKeyDown() || isAltGraphKeyDown();
    }

}
