package org.workcraft.formula;

import org.workcraft.formula.visitors.BooleanVisitor;

public final class Zero implements BooleanFormula {

    private static final Zero instance = new Zero();

    private Zero() {
    }

    public static Zero getInstance() {
        return instance;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
