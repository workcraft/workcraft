package org.workcraft.plugins.sdfs.stg;

import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public abstract class NodeSTG {
	public abstract List<VisualSignalTransition> getAllTransitions();

	public abstract List<VisualPlace> getAllPlaces();

	public boolean contains(Node n) {
		for (VisualPlace p: getAllPlaces()) {
			if (n == p || n == p.getReferencedPlace()) {
				return true;
			}
		}
		for (VisualSignalTransition t: getAllTransitions()) {
			if (n == t || n == t.getReferencedTransition()) {
				return true;
			}
		}
		return false;
	}
}
