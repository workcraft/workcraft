package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class ForkStg extends NodeStg {
	public final SignalStg i;
	public final SignalStg a;
	public final SignalStg b;
	public final SignalStg idn;
	public final SignalStg adn;
	public final SignalStg bdn;

	public ForkStg(SignalStg i, SignalStg a, SignalStg b, SignalStg idn, SignalStg adn, SignalStg bdn) {
		this.i = i;
		this.a = a;
		this.b = b;
		this.idn = idn;
		this.adn = adn;
		this.bdn = bdn;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> result = new HashSet<>();
		result.addAll(i.getAllTransitions());
		result.addAll(a.getAllTransitions());
		result.addAll(b.getAllTransitions());
		result.addAll(idn.getAllTransitions());
		result.addAll(adn.getAllTransitions());
		result.addAll(bdn.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		HashSet<VisualPlace> result = new HashSet<>();
		result.addAll(i.getAllPlaces());
		result.addAll(a.getAllPlaces());
		result.addAll(b.getAllPlaces());
		result.addAll(idn.getAllPlaces());
		result.addAll(adn.getAllPlaces());
		result.addAll(bdn.getAllPlaces());
		return result;
	}

}
