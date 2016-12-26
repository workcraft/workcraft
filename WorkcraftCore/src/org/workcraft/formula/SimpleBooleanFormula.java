package org.workcraft.formula;

public interface SimpleBooleanFormula {
    void accept(ReducedBooleanVisitor constantExpectingCnfGenerator);
}
