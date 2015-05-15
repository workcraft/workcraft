package org.workcraft.plugins.circuit.stg;

import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public abstract class NodeStg {
	public abstract List<VisualPlace> getAllVisualPlaces();
	public abstract List<VisualSignalTransition> getAllVisualTransitions();

	public boolean containsDirectlyOrByReference(Node n) {
		if (n != null) {
			for (VisualPlace p: getAllVisualPlaces()) {
				if ((n == p) || ((p != null) && (n == p.getReferencedPlace()))) {
					return true;
				}
			}
			for (VisualSignalTransition t: getAllVisualTransitions()) {
				if ((n == t) || ((t != null) && (n == t.getReferencedTransition()))) {
					return true;
				}
			}
		}
		return false;
	}

}
