package org.workcraft.plugins.xmas.stg;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;

public class FunctionStg extends NodeStg {
    public final ContactStg i;
    public final ContactStg o;

    public FunctionStg(ContactStg i, ContactStg o) {
        this.i = i;
        this.o = o;
    }

    @Override
    public List<VisualSignalTransition> getAllTransitions() {
        List<VisualSignalTransition> result = new ArrayList<>();
        result.addAll(i.getAllTransitions());
        result.addAll(o.getAllTransitions());
        return result;
    }

    @Override
    public List<VisualPlace> getAllPlaces() {
        List<VisualPlace> result = new ArrayList<>();
        result.addAll(i.getAllPlaces());
        result.addAll(o.getAllPlaces());
        return result;
    }

}
