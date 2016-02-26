package org.workcraft.workspace;

import java.util.Stack;

public class MementoManager {
    private final Stack<Memento> undoStack = new Stack<>();
    private final Stack<Memento> redoStack = new Stack<>();

    public void pushUndo(Memento memento) {
        this.undoStack.push(memento);
    }

    public Memento pullUndo() {
        return undoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.empty();
    }

    public void pushRedo(Memento memento) {
        this.redoStack.push(memento);
    }

    public Memento pullRedo() {
        return redoStack.pop();
    }

    public boolean canRedo() {
        return !redoStack.empty();
    }

    public Memento undo(Memento memento) {
        Memento result = null;
        if (canUndo()) {
            result = pullUndo();
            pushRedo(memento);
        }
        return result;
    }

    public Memento redo(Memento memento) {
        Memento result = null;
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
