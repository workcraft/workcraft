package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class JoinStg extends NodeStg {
	public final SignalStg a;
	public final SignalStg b;
	public final SignalStg o;

	public JoinStg(SignalStg a, SignalStg b, SignalStg o) {
		this.a = a;
		this.b = b;
		this.o = o;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> result = new HashSet<>();
		result.addAll(a.getAllTransitions());
		result.addAll(b.getAllTransitions());
		result.addAll(o.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		HashSet<VisualPlace> result = new HashSet<>();
		result.addAll(a.getAllPlaces());
		result.addAll(b.getAllPlaces());
		result.addAll(o.getAllPlaces());
		return result;
	}

}
