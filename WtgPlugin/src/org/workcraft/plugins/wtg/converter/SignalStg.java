package org.workcraft.plugins.wtg.converter;

import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.StgSettings;

import java.util.Arrays;
import java.util.Collection;

public class SignalStg {

    public final StgPlace zero;
    public final StgPlace one;
    public final StgPlace stable;
    public final StgPlace unstable;
    public final SignalTransition fall;
    public final SignalTransition rise;

    public SignalStg(StgPlace zero, StgPlace one, StgPlace stable, StgPlace unstable, SignalTransition fall, SignalTransition rise) {
        this.zero = zero;
        this.one = one;
        this.stable = stable;
        this.unstable = unstable;
        this.fall = fall;
        this.rise = rise;
    }

    public Collection<SignalTransition> getAllTransitions() {
        return Arrays.asList(fall, rise);
    }

    public Collection<StgPlace> getAllPlaces() {
        return Arrays.asList(zero, one, stable, unstable);
    }

    public static String getLowName(String signalName) {
        return signalName + StgSettings.getLowLevelSuffix();
    }

    public static String getHighName(String signalName) {
        return signalName + StgSettings.getHighLevelSuffix();
    }

    public static String getStableName(String signalName) {
        return signalName + "_stable";
    }

    public static String getUnstableName(String signalName) {
        return signalName + "_unstable";
    }

}
