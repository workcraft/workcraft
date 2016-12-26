package org.workcraft.formula;

public interface BooleanWorker {
    BooleanFormula one();
    BooleanFormula zero();

    BooleanFormula not(BooleanFormula x);

    BooleanFormula and(BooleanFormula x, BooleanFormula y);
    BooleanFormula or(BooleanFormula x, BooleanFormula y);
    BooleanFormula imply(BooleanFormula x, BooleanFormula y);
    BooleanFormula iff(BooleanFormula x, BooleanFormula y);
    BooleanFormula xor(BooleanFormula x, BooleanFormula y);
}
