package org.workcraft.plugins.cpog.formula;

public class Sequence extends BinaryCpogFormula {

    Sequence(CpogFormula x, CpogFormula y) {
        super(x, y);
    }

    @Override
    public <T> T accept(CpogVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
