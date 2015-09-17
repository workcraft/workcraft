package org.workcraft.plugins.dfs.stg;

import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public abstract class NodeStg {
	public abstract List<VisualSignalTransition> getAllTransitions();

	public abstract List<VisualPlace> getAllPlaces();

	public boolean contains(Node n) {
		if (n != null) {
			for (VisualPlace p: getAllPlaces()) {
				if (n == p || (p != null && n == p.getReferencedPlace())) {
					return true;
				}
			}
			for (VisualSignalTransition t: getAllTransitions()) {
				if (n == t || (t != null && n == t.getReferencedTransition())) {
					return true;
				}
			}
		}
		return false;
	}

}
