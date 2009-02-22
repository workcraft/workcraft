package org.workcraft.plugins.stg;

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

}
