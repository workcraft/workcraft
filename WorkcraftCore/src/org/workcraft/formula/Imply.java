package org.workcraft.formula;

public class Imply extends BinaryBooleanFormula {

    Imply(BooleanFormula x, BooleanFormula y) {
        super(x, y);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
