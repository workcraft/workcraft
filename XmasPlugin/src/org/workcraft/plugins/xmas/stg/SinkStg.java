package org.workcraft.plugins.xmas.stg;

import java.util.Collection;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SinkStg extends NodeStg {
	public final ContactStg i;

	public SinkStg(ContactStg i) {
		this.i = i;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		return i.getAllTransitions();
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		return i.getAllPlaces();
	}

}
