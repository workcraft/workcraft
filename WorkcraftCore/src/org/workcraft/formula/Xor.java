package org.workcraft.formula;

public class Xor extends BinaryBooleanFormula {
    public Xor(BooleanFormula x, BooleanFormula y) {
        super(x, y);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
