package org.workcraft.plugins.stg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Transition;

@DisplayName("Signal Transition Graph")
@VisualClass("org.workcraft.plugins.stg.VisualSTG")
public class STG extends PetriNet {
	public STG() {
		super();
		addSupportedComponents();
	}

	private void addSupportedComponents() {
		addComponentSupport(SignalTransition.class);
		removeComponentSupport(Transition.class);
	}

	public Set<SignalTransition>getSignalTransitions() {
		HashSet<SignalTransition> ret = new HashSet<SignalTransition>();

		for (Transition t : getTransitions())
			if (t instanceof SignalTransition)
				ret.add((SignalTransition)t);

		return ret;
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
		addComponent(ret);
		return ret;
	}
}

class InstanceCounter {
	public int plusCounter = 1;
	public int minusCounter = 1;
	public int toggleCounter = 1;
}