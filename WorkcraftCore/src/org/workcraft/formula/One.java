package org.workcraft.formula;

public final class One implements BooleanFormula {

    private static final One instance = new One();

    private One() {
    }

    public static One instance() {
        return instance;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
