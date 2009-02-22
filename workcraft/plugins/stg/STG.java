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
		HashMap<String, Integer> instanceCounter = new HashMap<String, Integer>();

		for (SignalTransition t : getSignalTransitions()) {
			String signalName = t.getSignalName();

			Integer instance = instanceCounter.get(signalName);
			if (instance == null)
				instance = new Integer(1);

			t.setInstance(instance);

			instanceCounter.put(signalName, ++instance);
		}
	}
}