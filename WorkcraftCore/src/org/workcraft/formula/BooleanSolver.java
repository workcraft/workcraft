package org.workcraft.formula;

public interface BooleanSolver<T> {
    BooleanSolution solve(T formula);
}
