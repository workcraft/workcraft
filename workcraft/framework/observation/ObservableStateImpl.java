package org.workcraft.framework.observation;

import java.util.HashSet;

public class ObservableStateImpl implements ObservableState {
	private HashSet<StateObserver> observers = new HashSet<StateObserver>();

	public void addObserver(StateObserver obs) {
		observers.add(obs);
	}

	public void removeObserver(StateObserver obs) {
		observers.remove(obs);
	}

	public void sendNotification (StateEvent e) {
		for (StateObserver o : observers)
			o.notify(e);
	}
}
