package org.workcraft.workspace;

import java.util.ArrayDeque;
import java.util.Deque;

public class MementoManager {
    private final Deque<Resource> undoStack = new ArrayDeque<>();
    private final Deque<Resource> redoStack = new ArrayDeque<>();

    public void pushUndo(Resource memento) {
        this.undoStack.push(memento);
    }

    public Resource pullUndo() {
        return undoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public void pushRedo(Resource memento) {
        this.redoStack.push(memento);
    }

    public Resource pullRedo() {
        return redoStack.pop();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
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
