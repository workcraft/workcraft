package org.workcraft.gui.events;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.utils.DesktopApi;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class GraphEditorKeyEvent {

    private final GraphEditor editor;
    private final char keyChar;
    private final int keyCode;
    private final int modifiers;

    public GraphEditorKeyEvent(GraphEditor editor, KeyEvent event) {
        this.editor = editor;
        keyChar = event.getKeyChar();
        keyCode = event.getKeyCode();
        modifiers = event.getModifiersEx();
    }

    public GraphEditor getEditor() {
        return editor;
    }

    public VisualModel getModel() {
        return editor.getModel();
    }

    public char getKeyChar() {
        return keyChar;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    private boolean isMaskHit(int mask) {
        return (getModifiers() & mask) == mask;
    }

    public boolean isCtrlKeyDown() {
        return isMaskHit(InputEvent.CTRL_DOWN_MASK);
    }

    public boolean isShiftKeyDown() {
        return isMaskHit(InputEvent.SHIFT_DOWN_MASK);
    }

    public boolean isAltKeyDown() {
        return isMaskHit(InputEvent.ALT_DOWN_MASK) || isMaskHit(InputEvent.ALT_GRAPH_DOWN_MASK);
    }

    public boolean isMetaKeyDown() {
        return isMaskHit(InputEvent.META_DOWN_MASK);
    }

    public boolean isMenuKeyDown() {
        if (DesktopApi.getMenuKeyMask() == ActionEvent.META_MASK) {
            return isMetaKeyDown();
        }
        return isCtrlKeyDown();
    }

}
