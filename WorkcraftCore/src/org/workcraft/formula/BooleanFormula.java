package org.workcraft.formula;

public interface BooleanFormula {
    <T> T accept(BooleanVisitor<T> visitor);
}
