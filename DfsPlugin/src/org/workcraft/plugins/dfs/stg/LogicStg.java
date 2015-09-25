package org.workcraft.plugins.dfs.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;

public class LogicStg extends NodeStg {
	public final VisualPlace C0;						// C=0
	public final VisualPlace C1;						// C=1
	public final Map<Node, VisualSignalTransition> CRs;	// C+
	public final Map<Node, VisualSignalTransition> CFs;	// C-

	public LogicStg(VisualPlace C0, VisualPlace C1, Map<Node, VisualSignalTransition> CRs, Map<Node, VisualSignalTransition> CFs) {
		this.C0 = C0;
		this.C1 = C1;
		this.CRs = CRs;
		this.CFs = CFs;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		Set<VisualSignalTransition> tmp = new HashSet<>();
		tmp.addAll(CRs.values());
		tmp.addAll(CFs.values());
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(tmp);
		return result;
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(C0, C1);
	}

}
