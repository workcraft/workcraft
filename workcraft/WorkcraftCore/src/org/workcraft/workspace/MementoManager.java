package org.workcraft.workspace;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntSupplier;

public class MementoManager {
    private final Deque<Resource> undoStack = new ArrayDeque<>();
    private final Deque<Resource> redoStack = new ArrayDeque<>();
    private final IntSupplier maxDepthSupplier;

    public MementoManager(IntSupplier maxDepthSupplier) {
        this.maxDepthSupplier = maxDepthSupplier;
    }

    public void pushUndo(Resource memento) {
        this.undoStack.push(memento);
        trim(maxDepthSupplier.getAsInt());
    }

    /**
     * Drop the steps that do not fit the depth: the oldest undo steps first, then the furthest redo steps.
     */
    public void trim(int maxDepth) {
        while (undoStack.size() > maxDepth) {
            undoStack.removeLast();
        }
        while (undoStack.size() + redoStack.size() > maxDepth) {
            redoStack.removeLast();
        }
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
