package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SourceStg extends NodeStg {
	public final ContactStg o;
	public final SignalStg oracle;

	public SourceStg(ContactStg o, SignalStg oracle) {
		this.o = o;
		this.oracle = oracle;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(o.getAllTransitions());
		result.addAll(oracle.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		result.addAll(o.getAllPlaces());
		result.addAll(oracle.getAllPlaces());
		return result;
	}

}
