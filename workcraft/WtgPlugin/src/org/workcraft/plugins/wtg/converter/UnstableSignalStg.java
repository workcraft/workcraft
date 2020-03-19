package org.workcraft.plugins.wtg.converter;

import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgPlace;

import java.util.Arrays;
import java.util.Collection;

public class UnstableSignalStg {

    public final StgPlace lowPlace;
    public final StgPlace highPlace;
    public final StgPlace stablePlace;
    public final StgPlace unstablePlace;
    public final SignalTransition fallTransition;
    public final SignalTransition riseTransition;

    public UnstableSignalStg(StgPlace lowPlace, StgPlace highPlace, SignalTransition fallTransition,
            SignalTransition riseTransition, StgPlace stablePlace, StgPlace unstablePlace) {
        this.lowPlace = lowPlace;
        this.highPlace = highPlace;
        this.fallTransition = fallTransition;
        this.riseTransition = riseTransition;
        this.stablePlace = stablePlace;
        this.unstablePlace = unstablePlace;
    }

    public Collection<SignalTransition> getAllTransitions() {
        return Arrays.asList(fallTransition, riseTransition);
    }

    public Collection<StgPlace> getAllPlaces() {
        return Arrays.asList(lowPlace, highPlace, stablePlace, unstablePlace);
    }

}
