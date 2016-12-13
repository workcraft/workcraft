package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.converter.NodeStg;
import org.workcraft.plugins.stg.converter.SignalStg;

public class SlotStg extends NodeStg {
    public final SignalStg mem;
    public final ContactStg hd;
    public final ContactStg tl;

    public SlotStg(SignalStg mem, ContactStg hd, ContactStg tl) {
        this.mem = mem;
        this.hd = hd;
        this.tl = tl;
    }

    @Override
    public Collection<VisualSignalTransition> getAllTransitions() {
        HashSet<VisualSignalTransition> result = new HashSet<>();
        result.addAll(mem.getAllTransitions());
        result.addAll(hd.getAllTransitions());
        result.addAll(tl.getAllTransitions());
        return result;
    }

    @Override
    public Collection<VisualPlace> getAllPlaces() {
        HashSet<VisualPlace> result = new HashSet<>();
        result.addAll(mem.getAllPlaces());
        result.addAll(hd.getAllPlaces());
        result.addAll(tl.getAllPlaces());
        return result;
    }

}
