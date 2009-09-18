package org.workcraft.observation;

public interface ObservableHierarchy {
	public void addObserver (HierarchyObserver obs);
	public void removeObserver (HierarchyObserver obs);
}
