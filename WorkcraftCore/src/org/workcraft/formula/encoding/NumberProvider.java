package org.workcraft.formula.encoding;

import org.workcraft.formula.BooleanFormula;

public interface NumberProvider<Formula> {
    Formula generate(String varPrefix, int range);
    BooleanFormula select(BooleanFormula[] vars, Formula number);
    BooleanFormula getConstraints();
    BooleanFormula less(Formula a, Formula b);
}
