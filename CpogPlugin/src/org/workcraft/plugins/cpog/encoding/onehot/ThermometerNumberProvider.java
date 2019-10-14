package org.workcraft.plugins.cpog.encoding.onehot;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.plugins.cpog.formula.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanWorker;
import org.workcraft.plugins.cpog.encoding.NumberProvider;

import java.util.ArrayList;
import java.util.List;

class ThermometerNumberProvider implements NumberProvider<ThermometerBooleanFormula> {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    private final List<BooleanFormula> rho = new ArrayList<>();

    @Override
    public ThermometerBooleanFormula generate(String varPrefix, int range) {
        List<BooleanVariable> vars = new ArrayList<>();
        for (int i = 0; i < range - 1; i++) {
            vars.add(new FreeVariable(varPrefix + "sel" + i));
        }

        for (int i = 0; i < range - 2; i++) {
            rho.add(WORKER.imply(vars.get(i + 1), vars.get(i)));
        }
        return new ThermometerBooleanFormula(vars);
    }

    @Override
    public BooleanFormula select(BooleanFormula[] vars, ThermometerBooleanFormula number) {
        List<BooleanVariable> digits = number.getVars();
        int n = digits.size();
        if (n + 1 != vars.length) {
            throw new RuntimeException("Lengths do not match");
        }
        if (n == 0) {
            return vars[0];
        }

        List<BooleanFormula> result = new ArrayList<>();
        result.add(WORKER.imply(WORKER.not(digits.get(0)), vars[0]));
        result.add(WORKER.imply(digits.get(n - 1), vars[n]));
        for (int i = 0; i < n - 1; i++) {
            result.add(WORKER.imply(WORKER.and(digits.get(i), WORKER.not(digits.get(i + 1))), vars[i + 1]));
        }
        return FormulaUtils.createAnd(result, WORKER);
    }

    @Override
    public BooleanFormula getConstraints() {
        return FormulaUtils.createAnd(rho, WORKER);
    }

    public BooleanFormula lessOrEquals(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
        List<BooleanFormula> conditions = new ArrayList<>();
        List<BooleanVariable> aVars = a.getVars();
        List<BooleanVariable> bVars = b.getVars();
        for (int i = 0; i < aVars.size(); i++) {
            conditions.add(WORKER.imply(aVars.get(i), bVars.get(i)));
        }
        return FormulaUtils.createAnd(conditions, WORKER);
    }

    @Override
    public BooleanFormula less(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
        return WORKER.not(lessOrEquals(b, a));
    }

}
