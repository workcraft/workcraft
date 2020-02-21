package org.workcraft.workspace;

import java.util.Stack;

public class MementoManager {
    private final Stack<RawData> undoStack = new Stack<>();
    private final Stack<RawData> redoStack = new Stack<>();

    public void pushUndo(RawData memento) {
        this.undoStack.push(memento);
    }

    public RawData pullUndo() {
        return undoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.empty();
    }

    public void pushRedo(RawData memento) {
        this.redoStack.push(memento);
    }

    public RawData pullRedo() {
        return redoStack.pop();
    }

    public boolean canRedo() {
        return !redoStack.empty();
    }

    public RawData undo(RawData memento) {
        RawData result = null;
        if (canUndo()) {
            result = pullUndo();
            pushRedo(memento);
        }
        return result;
    }

    public RawData redo(RawData memento) {
        RawData result = null;
        if (canRedo()) {
            result = pullRedo();
            pushUndo(memento);
        }
        return result;
    }

    public void clearRedo() {
        redoStack.clear();
    }

}
