package org.workcraft.plugins.petri;

import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.framework.observation.ObservableState;
import org.workcraft.framework.observation.ObservableStateImpl;
import org.workcraft.framework.observation.PropertyChangedEvent;
import org.workcraft.framework.observation.StateObserver;

@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends MathNode implements ObservableState {
	ObservableStateImpl observableStateImpl = new ObservableStateImpl();

	protected int tokens = 0;
	protected int capacity = 1;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int c) {
		this.capacity = c;
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;

		observableStateImpl.sendNotification( new PropertyChangedEvent(this, "tokens") );
	}

	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
	}
}