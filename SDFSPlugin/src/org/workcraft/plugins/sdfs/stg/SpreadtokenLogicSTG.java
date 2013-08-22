package org.workcraft.plugins.sdfs.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SpreadtokenLogicSTG extends NodeSTG {
	public final VisualPlace C0;
	public final VisualPlace C1;
	public final Map<Node, VisualSignalTransition> CRs;
	public final Map<Node, VisualSignalTransition> CFs;

	public SpreadtokenLogicSTG(VisualPlace C0, VisualPlace C1, Map<Node, VisualSignalTransition> CRs, Map<Node, VisualSignalTransition> CFs) {
		this.C0 = C0;
		this.C1 = C1;
		this.CRs = CRs;
		this.CFs = CFs;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		Set<VisualSignalTransition> tmp = new HashSet<VisualSignalTransition>();
		tmp.addAll(CRs.values());
		tmp.addAll(CFs.values());
		List<VisualSignalTransition> result = new ArrayList<VisualSignalTransition>();
		result.addAll(tmp);
		return result;
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(C0, C1);
	}
}