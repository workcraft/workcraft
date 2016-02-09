package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;

public class ComplexResult implements ConnectionResult {
    private final STGPlace implicitPlace;
    private final MathConnection con1;
    private final MathConnection con2;

    public ComplexResult(STGPlace place, MathConnection con1, MathConnection con2) {
        this.implicitPlace = place;
        this.con1 = con1;
        this.con2 = con2;
    }

    @Override
    public MathConnection getSimpleResult() {
        return null;
    }

    @Override
    public MathConnection getCon1() {
        return con1;
    }

    @Override
    public MathConnection getCon2() {
        return con2;
    }

    @Override
    public STGPlace getImplicitPlace() {
        return implicitPlace;
    }
}
