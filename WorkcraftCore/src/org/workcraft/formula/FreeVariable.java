package org.workcraft.formula;

import org.workcraft.formula.visitors.BooleanVisitor;

public class FreeVariable implements BooleanVariable, Comparable<FreeVariable> {

    private final String label;

    public FreeVariable(String label) {
        this.label = label;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int compareTo(FreeVariable var) {
        return -var.getLabel().compareTo(getLabel());
    }

}
