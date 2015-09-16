package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class ForkStg extends NodeStg {
	public final SignalStg i;
	public final SignalStg a;
	public final SignalStg b;

	public ForkStg(SignalStg i, SignalStg a, SignalStg b) {
		this.i = i;
		this.a = a;
		this.b = b;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> result = new HashSet<>();
		result.addAll(i.getAllTransitions());
		result.addAll(a.getAllTransitions());
		result.addAll(b.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		HashSet<VisualPlace> result = new HashSet<>();
		result.addAll(i.getAllPlaces());
		result.addAll(a.getAllPlaces());
		result.addAll(b.getAllPlaces());
		return result;
	}

}
