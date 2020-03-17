package org.workcraft.plugins.cpog.encoding;

import org.workcraft.formula.BooleanFormula;

public interface NumberProvider<T> {
    T generate(String varPrefix, int range);
    BooleanFormula select(BooleanFormula[] vars, T number);
    BooleanFormula getConstraints();
    BooleanFormula less(T a, T b);
}
