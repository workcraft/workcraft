package org.workcraft.workspace;

import java.util.Stack;

public class MementoManager {
    private final Stack<Resource> undoStack = new Stack<>();
    private final Stack<Resource> redoStack = new Stack<>();

    public void pushUndo(Resource memento) {
        this.undoStack.push(memento);
    }

    public Resource pullUndo() {
        return undoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.empty();
    }

    public void pushRedo(Resource memento) {
        this.redoStack.push(memento);
    }

    public Resource pullRedo() {
        return redoStack.pop();
    }

    public boolean canRedo() {
        return !redoStack.empty();
    }

    public Resource undo(Resource memento) {
        Resource result = null;
        if (canUndo()) {
            result = pullUndo();
            pushRedo(memento);
        }
        return result;
    }

    public Resource redo(Resource memento) {
        Resource result = null;
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
