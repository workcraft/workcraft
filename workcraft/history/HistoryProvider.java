package org.workcraft.history;
import java.util.List;

public interface HistoryProvider {
	public List<HistoryEvent> getHistory();

	public boolean canUndo();
	public void undo();
	public boolean canRedo();
	public void redo();

	public void moveToState (int index);
	public int getCurrentStateIndex();

	public void addHistoryListener(HistoryListener listener);
	public void removeHistoryListener(HistoryListener listener);
}