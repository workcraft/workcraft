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

public class LogicStg extends NodeStg {
    public final VisualPlace c0;                        // C=0
    public final VisualPlace c1;                        // C=1
    public final Map<Node, VisualSignalTransition> cRs;    // C+
    public final Map<Node, VisualSignalTransition> cFs;    // C-

    public LogicStg(VisualPlace c0, VisualPlace c1, Map<Node, VisualSignalTransition> cRs, Map<Node, VisualSignalTransition> cFs) {
        this.c0 = c0;
        this.c1 = c1;
        this.cRs = cRs;
        this.cFs = cFs;
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        Set<VisualSignalTransition> tmp = new HashSet<>();
        tmp.addAll(cRs.values());
        tmp.addAll(cFs.values());
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(tmp);
        return result;
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        return Arrays.asList(c0, c1);
    }

}
