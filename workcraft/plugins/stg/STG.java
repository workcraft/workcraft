package org.workcraft.plugins.stg;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Transition;

@DisplayName("Signal Transition Graph (STG)")
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
}
