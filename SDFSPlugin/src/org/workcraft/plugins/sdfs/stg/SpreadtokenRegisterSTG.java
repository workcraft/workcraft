package org.workcraft.plugins.sdfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SpreadtokenRegisterSTG extends NodeSTG {
	public final VisualPlace E0;
	public final VisualPlace E1;
	public final VisualSignalTransition ER;
	public final VisualSignalTransition EF;
	public final VisualPlace M0;
	public final VisualPlace M1;
	public final VisualSignalTransition MR;
	public final VisualSignalTransition MF;

	public SpreadtokenRegisterSTG(
			VisualPlace E0, VisualPlace E1, VisualSignalTransition ER, VisualSignalTransition EF,
			VisualPlace M0, VisualPlace M1, VisualSignalTransition MR, VisualSignalTransition MF) {
		this.E0 = E0;
		this.E1 = E1;
		this.ER = ER;
		this.EF = EF;
		this.M0 = M0;
		this.M1 = M1;
		this.MR = MR;
		this.MF = MF;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return Arrays.asList(ER, EF, MR, MF);
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(E0, E1, M0, M1);
	}

}