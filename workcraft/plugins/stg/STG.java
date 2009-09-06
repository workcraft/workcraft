package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.HashMap;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.util.Hierarchy;

@VisualClass("org.workcraft.plugins.stg.VisualSTG")
@DisplayName("Signal Transition Graph")
public class STG extends PetriNet {

	public STG() {
		super();

		new SignalTypeConsistencySupervisor(this).attach(getRoot());
	}

	public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	public void assignInstances() {
		HashMap<String, InstanceCounter> instanceCounterMap = new HashMap<String, InstanceCounter>();

		for (SignalTransition t : getSignalTransitions()) {
			String signalName = t.getSignalName();

			InstanceCounter instanceCounter = instanceCounterMap.get(signalName);

			if (instanceCounter == null)
				instanceCounter = new InstanceCounter();

			switch (t.getDirection()) {
			case PLUS:
				t.setInstance(instanceCounter.plusCounter++);
				break;
			case MINUS:
				t.setInstance(instanceCounter.minusCounter++);
				break;
			case TOGGLE:
				t.setInstance(instanceCounter.toggleCounter++);
				break;
			}

			instanceCounterMap.put(signalName, instanceCounter);
		}
	}

	final public SignalTransition createSignalTransition() {
		SignalTransition ret = new SignalTransition();
		getRoot().add(ret);
		return ret;
	}
}

class InstanceCounter {
	public int plusCounter = 1;
	public int minusCounter = 1;
	public int toggleCounter = 1;
}