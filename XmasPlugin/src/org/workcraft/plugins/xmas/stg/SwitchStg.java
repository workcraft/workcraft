package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SwitchStg extends NodeStg {
	public final ContactStg i;
	public final ContactStg a;
	public final ContactStg b;
	public final SignalStg oracle;

	public SwitchStg(ContactStg i, ContactStg a, ContactStg b, SignalStg oracle) {
		this.i = i;
		this.a = a;
		this.b = b;
		this.oracle = oracle;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> result = new HashSet<>();
		result.addAll(i.getAllTransitions());
		result.addAll(a.getAllTransitions());
		result.addAll(b.getAllTransitions());
		result.addAll(oracle.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		HashSet<VisualPlace> result = new HashSet<>();
		result.addAll(i.getAllPlaces());
		result.addAll(a.getAllPlaces());
		result.addAll(b.getAllPlaces());
		result.addAll(oracle.getAllPlaces());
		return result;
	}

}
