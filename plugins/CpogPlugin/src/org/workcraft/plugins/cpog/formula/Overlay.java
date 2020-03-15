package org.workcraft.plugins.cpog.formula;

public class Overlay extends BinaryCpogFormula {

    Overlay(CpogFormula x, CpogFormula y) {
        super(x, y);
    }

    @Override
    public <T> T accept(CpogVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
