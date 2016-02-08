package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;
import org.workcraft.plugins.stg.generator.SignalStg;

public class ContactStg extends NodeStg {
    public final SignalStg rdy;
    public final SignalStg dn;

    public ContactStg(SignalStg rdy, SignalStg dn) {
        this.rdy = rdy;
        this.dn = dn;
    }

    @Override
    public Collection<VisualSignalTransition> getAllTransitions() {
        HashSet<VisualSignalTransition> result = new HashSet<>();
        result.addAll(rdy.getAllTransitions());
        result.addAll(dn.getAllTransitions());
        return result;
    }

    @Override
    public Collection<VisualPlace> getAllPlaces() {
        HashSet<VisualPlace> result = new HashSet<>();
        result.addAll(rdy.getAllPlaces());
        result.addAll(dn.getAllPlaces());
        return result;
    }

}
