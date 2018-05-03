package org.workcraft.formula;

public interface BooleanWorker {
    // Constant
    BooleanFormula one();
    BooleanFormula zero();
    // Unary
    BooleanFormula not(BooleanFormula x);
    // Binary
    BooleanFormula and(BooleanFormula x, BooleanFormula y);
    BooleanFormula or(BooleanFormula x, BooleanFormula y);
    BooleanFormula xor(BooleanFormula x, BooleanFormula y);
    BooleanFormula imply(BooleanFormula x, BooleanFormula y);
    BooleanFormula iff(BooleanFormula x, BooleanFormula y);
}
