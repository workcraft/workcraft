package org.workcraft.plugins.sdfs.tools;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;

class LogicSTG {
	public final VisualPlace c0;
	public final VisualPlace c1;
	public final VisualSignalTransition cr;
	public final VisualSignalTransition cf;

	public LogicSTG(VisualPlace c0, VisualPlace c1, VisualSignalTransition cr, VisualSignalTransition cf) {
		this.c0 = c0;
		this.c1 = c1;
		this.cr = cr;
		this.cf = cf;
	}

	public boolean contains(Node n) {
		if (n instanceof VisualPlace) {
			VisualPlace p = (VisualPlace)n;
			return (p == c0 || p == c1);
		}
		if (n instanceof VisualSignalTransition) {
			VisualSignalTransition t = (VisualSignalTransition)n;
			return (t == cr || t == cf);
		}
		if (n instanceof Place) {
			Place p = (Place)n;
			return (p == c0.getPlace() || p == c1.getPlace());
		}
		if (n instanceof SignalTransition) {
			SignalTransition t = (SignalTransition)n;
			return (t == cr.getReferencedTransition() || t == cf.getReferencedTransition());
		}
		return false;
	}
}