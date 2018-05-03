package org.workcraft.formula;

public abstract class BinaryBooleanFormula implements BooleanFormula {

    private final BooleanFormula x;
    private final BooleanFormula y;

    public BinaryBooleanFormula(BooleanFormula x, BooleanFormula y) {
        this.x = x;
        this.y = y;
    }

    public BooleanFormula getX() {
        return x;
    }

    public BooleanFormula getY() {
        return y;
    }

}
