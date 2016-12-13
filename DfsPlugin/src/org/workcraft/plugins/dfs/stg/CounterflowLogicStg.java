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

public class CounterflowLogicStg extends NodeStg {
    public final VisualPlace fwC0;                            // forwardC=0
    public final VisualPlace fwC1;                            // forwardC=1
    public final Map<Node, VisualSignalTransition> fwCRs;    // forwardC+
    public final Map<Node, VisualSignalTransition>  fwCFs;    // forwardC-
    public final VisualPlace bwC0;                            // backwardC=0
    public final VisualPlace bwC1;                            // backwardC=1
    public final Map<Node, VisualSignalTransition>  bwCRs;    // backwardC+
    public final Map<Node, VisualSignalTransition>  bwCFs;    // backwardC-

    public CounterflowLogicStg(
            VisualPlace fwC0, VisualPlace fwC1, Map<Node, VisualSignalTransition>  fwCRs, Map<Node, VisualSignalTransition>  fwCFs,
            VisualPlace bwC0, VisualPlace bwC1, Map<Node, VisualSignalTransition>  bwCRs, Map<Node, VisualSignalTransition>  bwCFs) {
        this.fwC0 = fwC0;
        this.fwC1 = fwC1;
        this.fwCRs = fwCRs;
        this.fwCFs = fwCFs;
        this.bwC0 = bwC0;
        this.bwC1 = bwC1;
        this.bwCRs = bwCRs;
        this.bwCFs = bwCFs;
    }

    public List<VisualSignalTransition> getForwardTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(fwCRs.values());
        tmp.addAll(fwCFs.values());
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

    public List<VisualSignalTransition> getBackwardTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(bwCRs.values());
        tmp.addAll(bwCFs.values());
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(fwCRs.values());
        tmp.addAll(fwCFs.values());
        tmp.addAll(bwCRs.values());
        tmp.addAll(bwCFs.values());
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(fwC0, fwC1, bwC0, bwC1);
    }

}
