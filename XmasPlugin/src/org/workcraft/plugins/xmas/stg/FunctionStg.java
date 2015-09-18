package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class FunctionStg extends NodeStg {
	public final SignalStg i;
	public final SignalStg o;
	public final SignalStg idn;
	public final SignalStg odn;

	public FunctionStg(SignalStg i, SignalStg o, SignalStg idn, SignalStg odn) {
		this.i = i;
		this.o = o;
		this.idn = idn;
		this.odn = odn;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(i.getAllTransitions());
		result.addAll(o.getAllTransitions());
		result.addAll(idn.getAllTransitions());
		result.addAll(odn.getAllTransitions());
		return result;
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		List<VisualPlace> result = new ArrayList<>();
		result.addAll(i.getAllPlaces());
		result.addAll(o.getAllPlaces());
		result.addAll(idn.getAllPlaces());
		result.addAll(odn.getAllPlaces());
		return result;
	}

}
