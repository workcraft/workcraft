package org.workcraft.plugins.sdfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class RegisterSTG extends NodeSTG {
	public final VisualPlace M0;				// M=0
	public final VisualPlace M1;				// M=1
	public final VisualSignalTransition MR;	// M+
	public final VisualSignalTransition MF;	// M-
	public final VisualSignalTransition fMR;	// fakeM-
	public final VisualSignalTransition fMF;	// fakeM-

	public RegisterSTG(
			VisualPlace M0, VisualPlace M1, VisualSignalTransition MR, VisualSignalTransition MF,
			VisualSignalTransition fMR, VisualSignalTransition fMF) {
		this.M0 = M0;
		this.M1 = M1;
		this.MR = MR;
		this.MF = MF;
		this.fMR = fMR;
		this.fMF = fMF;
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return Arrays.asList(MR, MF, fMR, fMF);
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(M0, M1);
	}

}