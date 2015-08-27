package org.workcraft.plugins.xmas.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class ContactStg extends NodeStg {
	public final VisualPlace rdy0;
	public final VisualPlace rdy1;
	public final VisualSignalTransition rdyF;
	public final VisualSignalTransition rdyR;
//	public final HashSet<VisualSignalTransition> rdyFs;
//	public final HashSet<VisualSignalTransition> rdyRs;

	public ContactStg(VisualPlace rdy0, VisualPlace rdy1, VisualSignalTransition rdyF, VisualSignalTransition rdyR) {
		this.rdy0 = rdy0;
		this.rdy1 = rdy1;
		this.rdyF = rdyF;
		this.rdyR = rdyR;
//		this.rdyFs = new HashSet<>();
//		this.rdyFs.add(rdyF);
//		this.rdyRs = new HashSet<>();
//		this.rdyFs.add(rdyR);
	}

//	public ContactStg(VisualPlace p0, VisualPlace p1, HashSet<VisualSignalTransition> tFs, HashSet<VisualSignalTransition> tRs) {
//		this.rdy0 = p0;
//		this.rdy1 = p1;
//		this.rdyFs = tFs;
//		this.rdyRs = tRs;
//	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
//		Set<VisualSignalTransition> tmp = new HashSet<>();
//		tmp.addAll(rdyFs);
//		tmp.addAll(rdyRs);
//		List<VisualSignalTransition> result = new ArrayList<>();
//		result.addAll(tmp);
//		return result;
		return Arrays.asList(rdyF, rdyR);
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(rdy0, rdy1);
	}

}
