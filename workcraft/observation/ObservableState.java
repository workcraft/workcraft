package org.workcraft.observation;

public interface ObservableState {
	public void addObserver (StateObserver obs);
	public void removeObserver (StateObserver obs);
}
