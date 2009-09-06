package org.workcraft.framework.observation;

public interface ObservableHierarchy {
	public void addObserver (HierarchyObserver obs);
	public void removeObserver (HierarchyObserver obs);
}
