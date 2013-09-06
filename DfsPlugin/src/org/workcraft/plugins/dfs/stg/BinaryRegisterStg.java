package org.workcraft.plugins.dfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class BinaryRegisterStg extends NodeStg {
	public final VisualPlace M0;				// M=0
	public final VisualPlace M1;				// M=1
	public final VisualPlace tM0;				// trueM=0
	public final VisualPlace tM1;				// trueM=1
	public final VisualSignalTransition tMR;	// trueM+
	public final VisualSignalTransition tMF;	// trueM-
	public final VisualPlace fM0;				// falseM=0
	public final VisualPlace fM1;				// falseM=1
	public final VisualSignalTransition fMR;	// falseM+
	public final VisualSignalTransition fMF;	// falseM-

	public BinaryRegisterStg(VisualPlace M0, VisualPlace M1,
			VisualPlace tM0, VisualPlace tM1, VisualSignalTransition tMR, VisualSignalTransition tMF,
			VisualPlace fM0, VisualPlace fM1, VisualSignalTransition fMR, VisualSignalTransition fMF) {
		this.M0 = M0;
		this.M1 = M1;
		this.tM0 = tM0;
		this.tM1 = tM1;
		this.tMR = tMR;
		this.tMF = tMF;
		this.fM0 = fM0;
		this.fM1 = fM1;
		this.fMR = fMR;
		this.fMF = fMF;
	}

	public List<VisualSignalTransition> getTrueTransitions() {
		return Arrays.asList(tMR, tMF);
	}

	public List<VisualSignalTransition> getFalseTransitions() {
		return Arrays.asList(fMR, fMF);
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return Arrays.asList(tMR, tMF, fMR, fMF);
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(tM0, tM1, fM0, fM1, M0, M1);
	}

}