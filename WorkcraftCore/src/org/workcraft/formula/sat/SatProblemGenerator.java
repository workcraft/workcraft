package org.workcraft.formula.sat;

import org.workcraft.formula.BooleanVariable;

public interface SatProblemGenerator<T> {
    OptimisationTask<T> getFormula(String[] scenarios, BooleanVariable[] variables, int derivedVariables);
}
