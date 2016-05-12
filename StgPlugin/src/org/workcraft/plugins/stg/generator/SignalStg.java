package org.workcraft.plugins.stg.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class SignalStg extends NodeStg {

    private static final String _NAME0 = "_0";
    private static final String _NAME1 = "_1";

    public final VisualPlace zero;
    public final VisualPlace one;
    public final ArrayList<VisualSignalTransition> fallList = new ArrayList<>();
    public final ArrayList<VisualSignalTransition> riseList = new ArrayList<>();

    public SignalStg(VisualPlace zero, VisualPlace one) {
        this.zero = zero;
        this.one = one;
    }

    public SignalStg(VisualPlace zero, VisualPlace one, VisualSignalTransition fall, VisualSignalTransition rise) {
        this.zero = zero;
        this.one = one;
        this.fallList.add(fall);
        this.riseList.add(rise);
    }

    public SignalStg(VisualPlace zero, VisualPlace one, ArrayList<VisualSignalTransition> fallList, ArrayList<VisualSignalTransition> riseList) {
        this.zero = zero;
        this.one = one;
        this.fallList.addAll(fallList);
        this.riseList.addAll(riseList);
    }

    @Override
    public Collection<VisualSignalTransition> getAllTransitions() {
        HashSet<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(fallList);
        tmp.addAll(riseList);
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

    @Override
    public Collection<VisualPlace> getAllPlaces() {
        return Arrays.asList(zero, one);
    }

    public static String getLowName(String signalName) {
        return signalName + StgSettings.getLowLevelSuffix();
    }

    public static String getHighName(String signalName) {
        return signalName + StgSettings.getHighLevelSuffix();
    }

}
