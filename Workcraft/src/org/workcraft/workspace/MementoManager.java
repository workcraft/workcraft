package org.workcraft.workspace;

import java.util.Stack;

public class MementoManager {
	private final Stack<byte[]> undoStack = new Stack<byte[]>();
	private final Stack<byte[]> redoStack = new Stack<byte[]>();

	public void pushUndo(byte[] memento) {
		this.undoStack.push(memento);
	}

	public byte[] pullUndo() {
		return undoStack.pop();
	}

	public boolean canUndo() {
		return !undoStack.empty();
	}

	public void pushRedo(byte[] memento) {
		this.redoStack.push(memento);
	}

	public byte[] pullRedo() {
		return redoStack.pop();
	}

	public boolean canRedo() {
		return !redoStack.empty();
	}

	public byte[] undo(byte[] memento) {
		byte[] result = null;
		if (canUndo()) {
			result = pullUndo();
			pushRedo(memento);
		}
		return result;
	}

	public byte[] redo(byte[] memento) {
		byte[] result = null;
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
