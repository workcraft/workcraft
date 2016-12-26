package org.workcraft.formula;

public class And extends BinaryBooleanFormula implements SimpleBooleanFormula {

    And(BooleanFormula x, BooleanFormula y) {
        super(x, y);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(ReducedBooleanVisitor visitor) {
        visitor.visit(this);
    }
}
