package org.workcraft.plugins.dfs.stg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converter.NodeStg;

public class BinaryRegisterStg extends NodeStg {
    public final VisualPlace m0;                // M=0
    public final VisualPlace m1;                // M=1
    public final VisualPlace tM0;                // trueM=0
    public final VisualPlace tM1;                // trueM=1
    public final Map<Node, VisualSignalTransition> tMRs;    // trueM+
    public final VisualSignalTransition tMF;    // trueM-
    public final VisualPlace fM0;                // falseM=0
    public final VisualPlace fM1;                // falseM=1
    public final Map<Node, VisualSignalTransition> fMRs;    // trueM+
    public final VisualSignalTransition fMF;    // trueM-

    public BinaryRegisterStg(VisualPlace m0, VisualPlace m1,
            VisualPlace tM0, VisualPlace tM1,
            Map<Node, VisualSignalTransition> tMRs, VisualSignalTransition tMF,
            VisualPlace fM0, VisualPlace fM1,
            Map<Node, VisualSignalTransition> fMRs, VisualSignalTransition fMF) {
        this.m0 = m0;
        this.m1 = m1;
        this.tM0 = tM0;
        this.tM1 = tM1;
        this.tMRs = tMRs;
        this.tMF = tMF;
        this.fM0 = fM0;
        this.fM1 = fM1;
        this.fMRs = fMRs;
        this.fMF = fMF;
    }

    public List<VisualSignalTransition> getTrueTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>(tMRs.values());
        tmp.add(tMF);
        return new ArrayList<VisualSignalTransition>(tmp);
    }

    public List<VisualSignalTransition> getFalseTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>(fMRs.values());
        tmp.add(fMF);
        return new ArrayList<VisualSignalTransition>(tmp);
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(tMRs.values());
        tmp.add(tMF);
        tmp.addAll(fMRs.values());
        tmp.add(fMF);
        return new ArrayList<VisualSignalTransition>(tmp);
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(tM0, tM1, fM0, fM1, m0, m1);
    }

}