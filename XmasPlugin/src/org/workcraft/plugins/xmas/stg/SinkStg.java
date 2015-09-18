package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SinkStg extends NodeStg {
	public final SignalStg i;
	public final SignalStg oracle;
	public final SignalStg dn;

	public SinkStg(SignalStg i, SignalStg oracle, SignalStg dn) {
		this.i = i;
		this.oracle = oracle;
		this.dn = dn;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(i.getAllTransitions());
		result.addAll(oracle.getAllTransitions());
		result.addAll(dn.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		result.addAll(i.getAllPlaces());
		result.addAll(oracle.getAllPlaces());
		result.addAll(dn.getAllPlaces());
		return result;
	}

}
