package org.workcraft.plugins.xmas.stg;

import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SinkStg extends NodeStg {
	public final ContactStg i;

	public SinkStg(ContactStg i) {
		this.i = i;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return i.getAllTransitions();
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return i.getAllPlaces();
	}

}
