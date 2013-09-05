package org.workcraft.plugins.sdfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class CounterflowRegisterSTG extends NodeSTG {
	public final VisualPlace fwE0;				// forwardE=0
	public final VisualPlace fwE1;				// forwardE=1
	public final VisualSignalTransition fwER;	// forwardE+
	public final VisualSignalTransition fwEF;	// forwardE-
	public final VisualPlace bwE0;				// backwardE=0
	public final VisualPlace bwE1;				// backwardE=1
	public final VisualSignalTransition bwER;	// backwardE+
	public final VisualSignalTransition bwEF;	// backwardE-
	public final VisualPlace orM0;				// orM=0
	public final VisualPlace orM1;				// orM=1
	public final VisualSignalTransition orMRfw;// orMforward+
	public final VisualSignalTransition orMRbw;// orMbackward+
	public final VisualSignalTransition orMFfw;// orMforward-
	public final VisualSignalTransition orMFbw;// orMbackward-
	public final VisualPlace andM0;			// andMforward=0
	public final VisualPlace andM1;			// andMforward=1
	public final VisualSignalTransition andMR;	// andMforward+
	public final VisualSignalTransition andMF;	// andMforward-

	public CounterflowRegisterSTG(
			VisualPlace fwE0, VisualPlace fwE1, VisualSignalTransition fwER, VisualSignalTransition fwEF,
			VisualPlace bwE0, VisualPlace bwE1, VisualSignalTransition bwER, VisualSignalTransition bwEF,
			VisualPlace orM0, VisualPlace orM1, VisualSignalTransition orMRfw, VisualSignalTransition orMRbw, VisualSignalTransition orMFfw, VisualSignalTransition orMFbw,
			VisualPlace andM0, VisualPlace andM1, VisualSignalTransition andMR, VisualSignalTransition andMF) {
		this.fwE0 = fwE0;
		this.fwE1 = fwE1;
		this.fwER = fwER;
		this.fwEF = fwEF;
		this.bwE0 = bwE0;
		this.bwE1 = bwE1;
		this.bwER = bwER;
		this.bwEF = bwEF;
		this.orM0 = orM0;
		this.orM1 = orM1;
		this.orMRfw = orMRfw;
		this.orMRbw = orMRbw;
		this.orMFfw = orMFfw;
		this.orMFbw = orMFbw;
		this.andM0 = andM0;
		this.andM1 = andM1;
		this.andMR = andMR;
		this.andMF = andMF;
	}

	public  List<VisualSignalTransition> getForwardTransitions() {
		return Arrays.asList(fwER, fwEF);
	}

	public  List<VisualSignalTransition> getBackwardTransitions() {
		return Arrays.asList(bwER, bwEF);
	}

	public  List<VisualSignalTransition> getOrTransitions() {
		return Arrays.asList(orMRfw, orMFfw, orMRbw, orMFbw);
	}

	public  List<VisualSignalTransition> getAndTransitions() {
		return Arrays.asList(andMR, andMF);
	}

	@Override
	public List<VisualSignalTransition> getAllTransitions() {
		return Arrays.asList(fwER, fwEF, bwER, bwEF, orMRfw, orMFfw, orMRbw, orMFbw, andMR, andMF);
	}

	@Override
	public List<VisualPlace> getAllPlaces() {
		return Arrays.asList(fwE0, fwE1, bwE0, bwE1, orM0, orM1, andM0, andM1);
	}
}
