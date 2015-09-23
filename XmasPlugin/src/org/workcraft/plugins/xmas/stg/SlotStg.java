package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SlotStg extends NodeStg {
	public final SignalStg mem;
	public final SignalStg hd;
	public final SignalStg tl;
	public final SignalStg dn;

	public SlotStg(SignalStg mem, SignalStg hd, SignalStg tl, SignalStg dn) {
		this.mem = mem;
		this.hd = hd;
		this.tl = tl;
		this.dn = dn;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> result = new HashSet<>();
		result.addAll(mem.getAllTransitions());
		result.addAll(hd.getAllTransitions());
		result.addAll(tl.getAllTransitions());
		result.addAll(dn.getAllTransitions());
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		HashSet<VisualPlace> result = new HashSet<>();
		result.addAll(mem.getAllPlaces());
		result.addAll(hd.getAllPlaces());
		result.addAll(tl.getAllPlaces());
		result.addAll(dn.getAllPlaces());
		return result;
	}

}
