package org.workcraft.plugins.xmas.stg;

import java.util.Collection;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SourceStg extends NodeStg {
	public final ContactStg o;

	public SourceStg(ContactStg o) {
		this.o = o;
	}

	@Override
	public Collection<VisualSignalTransition> getAllTransitions() {
		return o.getAllTransitions();
	}

	@Override
	public Collection<VisualPlace> getAllPlaces() {
		return o.getAllPlaces();
	}

}
