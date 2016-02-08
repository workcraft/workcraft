package org.workcraft.plugins.xmas.stg;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.NodeStg;

public class JoinStg extends NodeStg {
    public final ContactStg a;
    public final ContactStg b;
    public final ContactStg o;

    public JoinStg(ContactStg a, ContactStg b, ContactStg o) {
        this.a = a;
        this.b = b;
        this.o = o;
    }

    @Override
    public Collection<VisualSignalTransition> getAllTransitions() {
        HashSet<VisualSignalTransition> result = new HashSet<>();
        result.addAll(a.getAllTransitions());
        result.addAll(b.getAllTransitions());
        result.addAll(o.getAllTransitions());
        return result;
    }

    @Override
    public Collection<VisualPlace> getAllPlaces() {
        HashSet<VisualPlace> result = new HashSet<>();
        result.addAll(a.getAllPlaces());
        result.addAll(b.getAllPlaces());
        result.addAll(o.getAllPlaces());
        return result;
    }

}
