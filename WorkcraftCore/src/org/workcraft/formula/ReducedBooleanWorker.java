package org.workcraft.formula;

public interface ReducedBooleanWorker {
    BooleanFormula and(BooleanFormula x, BooleanFormula y);
    BooleanFormula not(BooleanFormula x);
    BooleanFormula iff(BooleanFormula x, BooleanFormula y);
}
