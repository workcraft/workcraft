package org.workcraft.framework.observation;

public interface ObservableState {
	public void addObserver (StateObserver obs);
	public void removeObserver (StateObserver obs);
}
