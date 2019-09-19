package org.workcraft.formula;

public final class One implements BooleanFormula {

    private static final One instance = new One();

    private One() {
    }

    public static One getInstance() {
        return instance;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
