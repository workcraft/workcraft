package org.workcraft.gui.events;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.graph.tools.GraphEditor;

public class GraphEditorMouseEvent {

    GraphEditor editor;

    MouseEvent event;
    Point2D position;
    Point2D prevPosition;
    Point2D startPosition;
//    int button;
//    int clickCount;
//    int modifiers;

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

/*    public GraphEditorMouseEvent(GraphEditor editor, int event, Point2D position, int button, int clickCount, int modifiers) {
        this.editor = editor;
        this.event = event;
        this.position = position;
        this.button = button;
        this.clickCount = clickCount;
        this.modifiers = modifiers;
    }*/

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

    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
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
}
