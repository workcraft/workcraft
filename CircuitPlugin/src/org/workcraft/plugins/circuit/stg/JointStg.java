package org.workcraft.plugins.circuit.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class JointStg extends NodeStg {
	public final VisualPlace P0;
	public final VisualPlace P1;

	public JointStg(VisualPlace P0, VisualPlace P1) {
		this.P0 = P0;
		this.P1 = P1;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return new ArrayList<VisualSignalTransition>();
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(P0, P1);
	}

}
