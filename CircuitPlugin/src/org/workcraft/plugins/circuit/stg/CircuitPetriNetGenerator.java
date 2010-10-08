package org.workcraft.plugins.circuit.stg;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.STG;

public class CircuitPetriNetGenerator {
	public static STG generate(Circuit circuit) {
		try {
			STG stg = new STG();
			Transition t1 = stg.createTransition();
			Transition t2 = stg.createTransition();
			stg.connect(t1, t2);

			return stg;
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}

	}
}
