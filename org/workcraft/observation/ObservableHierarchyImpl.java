package org.workcraft.observation;

import java.util.HashSet;

public class ObservableHierarchyImpl implements ObservableHierarchy {
	private HashSet<HierarchyObserver> observers = new HashSet<HierarchyObserver>();

	public void addObserver(HierarchyObserver obs) {
		observers.add(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observers.remove(obs);
	}

	public void sendNotification (HierarchyEvent e) {
		for (HierarchyObserver obs : observers)
			obs.notify(e);
	}
}
