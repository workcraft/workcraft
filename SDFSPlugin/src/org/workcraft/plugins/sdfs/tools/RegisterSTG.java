package org.workcraft.plugins.sdfs.tools;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;

class RegisterSTG {
	public final VisualPlace e0;
	public final VisualPlace e1;
	public final VisualSignalTransition er;
	public final VisualSignalTransition ef;
	public final VisualPlace m0;
	public final VisualPlace m1;
	public final VisualSignalTransition mr;
	public final VisualSignalTransition mf;

	public RegisterSTG(
			VisualPlace e0, VisualPlace e1, VisualSignalTransition er, VisualSignalTransition ef,
			VisualPlace m0, VisualPlace m1, VisualSignalTransition mr, VisualSignalTransition mf) {
		this.e0 = e0;
		this.e1 = e1;
		this.er = er;
		this.ef = ef;
		this.m0 = m0;
		this.m1 = m1;
		this.mr = mr;
		this.mf = mf;
	}

	public boolean contains(Node n) {
		if (n instanceof VisualPlace) {
			VisualPlace p = (VisualPlace)n;
			return (p == e0 || p == e1 || p == m0 || p == m1);
		}
		if (n instanceof VisualSignalTransition) {
			VisualSignalTransition t = (VisualSignalTransition)n;
			return (t == er || t == ef || t == mr || t == mf);
		}
		if (n instanceof Place) {
			Place p = (Place)n;
			return (p == e0.getPlace() || p == e1.getPlace() || p == m0.getPlace() || p == m1.getPlace());
		}
		if (n instanceof SignalTransition) {
			SignalTransition t = (SignalTransition)n;
			return (t == er.getReferencedTransition() || t == ef.getReferencedTransition()
					|| t == mr.getReferencedTransition() || t == mf.getReferencedTransition());
		}

		return false;
	}
}