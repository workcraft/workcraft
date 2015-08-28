package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class ContactStg extends NodeStg {
	public final VisualPlace rdy0;
	public final VisualPlace rdy1;
	public final ArrayList<VisualSignalTransition> rdyFs = new ArrayList<>();
	public final ArrayList<VisualSignalTransition> rdyRs = new ArrayList<>();

	public ContactStg(VisualPlace rdy0, VisualPlace rdy1, VisualSignalTransition rdyF, VisualSignalTransition rdyR) {
		this.rdy0 = rdy0;
		this.rdy1 = rdy1;
		this.rdyFs.add(rdyF);
		this.rdyFs.add(rdyR);
	}

	public ContactStg(VisualPlace rdy0, VisualPlace rdy1, ArrayList<VisualSignalTransition> rdyFs, ArrayList<VisualSignalTransition> rdyRs) {
		this.rdy0 = rdy0;
		this.rdy1 = rdy1;
		this.rdyFs.addAll(rdyFs);
		this.rdyRs.addAll(rdyRs);
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		HashSet<VisualSignalTransition> tmp = new HashSet<>();
		tmp.addAll(rdyFs);
		tmp.addAll(rdyRs);
		List<VisualSignalTransition> result = new ArrayList<>();
		result.addAll(tmp);
		return result;
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		return Arrays.asList(rdy0, rdy1);
	}

}
