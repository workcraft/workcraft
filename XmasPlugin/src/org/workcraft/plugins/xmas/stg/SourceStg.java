package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SourceStg extends NodeStg {
	public final SignalStg o;
	public final SignalStg oracle;
	public final SignalStg dn;

	public SourceStg(SignalStg o, SignalStg oracle, SignalStg dn) {
		this.o = o;
		this.oracle = oracle;
		this.dn = dn;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(o.getAllTransitions());
		result.addAll(oracle.getAllTransitions());
		result.addAll(dn.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		result.addAll(o.getAllPlaces());
		result.addAll(oracle.getAllPlaces());
		result.addAll(dn.getAllPlaces());
		return result;
	}

}
