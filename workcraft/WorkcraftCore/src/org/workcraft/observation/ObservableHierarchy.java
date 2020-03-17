package org.workcraft.observation;

public interface ObservableHierarchy {
    void addObserver(HierarchyObserver obs);
    void removeObserver(HierarchyObserver obs);
    void removeAllObservers();
}
