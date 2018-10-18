package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;

public class ImplicitPlaceConnection extends MathConnection {

    private final StgPlace implicitPlace;

    public ImplicitPlaceConnection(StgPlace place, MathConnection first, MathConnection second) {
        this.implicitPlace = place;
        setDependencies(first, second);
    }

    @Override
    public MathConnection getFirst() {
        return (MathConnection) super.getFirst();
    }

    @Override
    public MathConnection getSecond() {
        return (MathConnection) super.getSecond();
    }

    public StgPlace getImplicitPlace() {
        return implicitPlace;
    }

}
