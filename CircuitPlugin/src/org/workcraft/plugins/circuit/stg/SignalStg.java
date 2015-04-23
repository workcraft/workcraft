package org.workcraft.plugins.circuit.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;


public class SignalStg extends NodeStg {
	public final VisualPlace P0;
	public final VisualPlace P1;
	public final HashSet<VisualSignalTransition> Rs;
	public final HashSet<VisualSignalTransition> Fs;

	public SignalStg(VisualPlace P0, VisualPlace P1) {
		this(P0, P1, new HashSet<VisualSignalTransition>(), new HashSet<VisualSignalTransition>());
	}

	public SignalStg(VisualPlace P0, VisualPlace P1, HashSet<VisualSignalTransition> Rs, HashSet<VisualSignalTransition> Fs) {
		this.P0 = P0;
		this.P1 = P1;
		this.Rs = Rs;
		this.Fs = Fs;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<VisualSignalTransition>();
		result.addAll(Rs);
		result.addAll(Fs);
		return result;
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(P0, P1);
	}

}
