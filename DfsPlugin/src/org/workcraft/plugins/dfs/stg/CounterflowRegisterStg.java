package org.workcraft.plugins.dfs.stg;

import java.util.Arrays;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converter.NodeStg;

public class CounterflowRegisterStg extends NodeStg {
    public final VisualPlace orM0;                // orM=0
    public final VisualPlace orM1;                // orM=1
    public final VisualSignalTransition orMRfw; // orMforward+
    public final VisualSignalTransition orMRbw; // orMbackward+
    public final VisualSignalTransition orMFfw; // orMforward-
    public final VisualSignalTransition orMFbw; // orMbackward-
    public final VisualPlace andM0;            // andMforward=0
    public final VisualPlace andM1;            // andMforward=1
    public final VisualSignalTransition andMR;    // andMforward+
    public final VisualSignalTransition andMF;    // andMforward-

    public CounterflowRegisterStg(
            VisualPlace orM0, VisualPlace orM1, VisualSignalTransition orMRfw, VisualSignalTransition orMRbw, VisualSignalTransition orMFfw, VisualSignalTransition orMFbw,
            VisualPlace andM0, VisualPlace andM1, VisualSignalTransition andMR, VisualSignalTransition andMF) {
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

    public  List<VisualSignalTransition> getOrTransitions() {
        return Arrays.asList(orMRfw, orMFfw, orMRbw, orMFbw);
    }

    public  List<VisualSignalTransition> getForwardTransitions() {
        return Arrays.asList(orMRfw, orMFfw, andMR, andMR);
    }

    public  List<VisualSignalTransition> getBackwardTransitions() {
        return Arrays.asList(orMRbw, orMFbw, andMR, andMR);
    }

    public  List<VisualSignalTransition> getAndTransitions() {
        return Arrays.asList(andMR, andMF);
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        return Arrays.asList(orMRfw, orMFfw, orMRbw, orMFbw, andMR, andMF);
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(orM0, orM1, andM0, andM1);
    }

}
