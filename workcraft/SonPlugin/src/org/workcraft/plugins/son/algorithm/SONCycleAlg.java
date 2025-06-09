package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.util.Phase;

public class SONCycleAlg extends BSONCycleAlg {

    private final SON net;

    public SONCycleAlg(SON net, Map<Condition, Collection<Phase>> phases) {
        super(net, phases);
        this.net = net;
    }

    /**
     *     get all cycles without synchronous cycles
     */
    @Override
    public Collection<Path> cycleTask(Collection<? extends Node> nodes) {
        return cycleFliter(super.cycleTask(nodes));
    }

    @Override
    protected Collection<Path> cycleFliter(Collection<Path> cycles) {
        List<Path> delList = new ArrayList<>();
        for (Path cycle : cycles) {
            //no causal relation involved
            if (!net.getSONConnectionTypes(cycle).contains(Semantics.PNLINE)) {
                int upper = 0;
                int lower = 0;
                for (Node n : cycle) {
                    if (!(n instanceof ChannelPlace)) {
                        if (bsonAlg.isUpperNode(n)) {
                            upper++;
                        } else {
                            lower++;
                        }
                    }
                }
                //all cycle nodes are in the same level
                if (upper == 0 || lower == 0) {
                    delList.add(cycle);
                }
            }
        }
        cycles.removeAll(delList);

        return cycles;
    }
}
