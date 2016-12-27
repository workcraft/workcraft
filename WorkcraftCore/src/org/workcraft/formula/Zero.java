package org.workcraft.formula;

public final class Zero implements BooleanFormula {

    private static final Zero instance = new Zero();

    private Zero() {
    }

    public static Zero instance() {
        return instance;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
