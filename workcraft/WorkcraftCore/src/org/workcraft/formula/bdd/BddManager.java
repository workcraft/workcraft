package org.workcraft.formula.bdd;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;

import java.util.Map;
import java.util.Set;

public interface BddManager extends AutoCloseable {

    String VARIABLE_COUNT_KEY = "Variable count";
    String REFERENCED_BDD_COUNT_KEY = "Referenced BDD count";

    // Override AutoCloseable.close() without throwing exceptions
    @Override
    void close();

    // Equivalence checks
    boolean isEquivalent(BooleanFormula leftFormula, BooleanFormula rightFormula);
    boolean isEquivalentToConstant(BooleanFormula funcFormula);
    boolean isEquivalentToConstant0(BooleanFormula funcFormula);
    boolean isEquivalentToConstant1(BooleanFormula funcFormula);

    // Unateness checks
    boolean isBinate(BooleanFormula formula, BooleanVariable var);
    boolean isPositiveUnate(BooleanFormula funcFormula, BooleanVariable var);
    boolean isNegativeUnate(BooleanFormula funcFormula, BooleanVariable var);

    // Formula transformations
    Set<BooleanVariable> calcRedundantVariables(BooleanFormula formula);
    BooleanFormula calcDistinctFormula(BooleanFormula formula);
    BooleanFormula calcProjectionFormula(BooleanFormula funcFormula, BooleanFormula caresetFormula);

    // Statistics
    Map<String, Integer> getStats();

    default void printStats(String intro) {
        System.out.println(intro);
        for (Map.Entry<String, Integer> entry : getStats().entrySet()) {
            System.out.println("* " + entry.getKey() + ": " + entry.getValue());
        }
    }
}
