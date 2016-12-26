package org.workcraft.formula;

public class Not implements BooleanFormula {

    private final BooleanFormula x;

    public Not(BooleanFormula x) {
        this.x = x;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public BooleanFormula getX() {
        return x;
    }

}
