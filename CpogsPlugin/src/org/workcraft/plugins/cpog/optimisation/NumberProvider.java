package org.workcraft.plugins.cpog.optimisation;

interface NumberProvider<Formula> {
    Formula generate(String varPrefix, int range);
    BooleanFormula select(BooleanFormula[] vars, Formula number);
    BooleanFormula getConstraints();
    BooleanFormula less(Formula a, Formula b);
}
