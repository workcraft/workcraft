package org.workcraft.plugins.cpog.encoding.onehot;

import static org.workcraft.formula.BooleanOperations.and;
import static org.workcraft.formula.BooleanOperations.imply;
import static org.workcraft.formula.BooleanOperations.not;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.plugins.cpog.encoding.NumberProvider;

class ThermometerNumberProvider implements NumberProvider<ThermometerBooleanFormula> {
    private final List<BooleanFormula> rho = new ArrayList<>();

    ThermometerNumberProvider() {
    }

    @Override
    public ThermometerBooleanFormula generate(String varPrefix, int range) {
        List<BooleanVariable> vars = new ArrayList<>();
        for (int i = 0; i < range - 1; i++) {
            vars.add(new FreeVariable(varPrefix + "sel" + i));
        }

        for (int i = 0; i < range - 2; i++) {
            rho.add(imply(vars.get(i + 1), vars.get(i)));
        }

        return new ThermometerBooleanFormula(vars);
    }

    @Override
    public BooleanFormula select(BooleanFormula[] vars,
            ThermometerBooleanFormula number) {
        List<BooleanFormula> conditions = new ArrayList<>();

        List<BooleanVariable> digits = number.getVars();
        int n = digits.size();
        if (n + 1 != vars.length) {
            throw new RuntimeException("Lengths do not match");
        }
        if (n == 0) {
            return vars[0];
        }

        conditions.add(imply(not(digits.get(0)), vars[0]));
        conditions.add(imply(digits.get(n - 1), vars[n]));
        for (int i = 0; i < n - 1; i++) {
            conditions.add(imply(and(digits.get(i), not(digits.get(i + 1))), vars[i + 1]));
        }

        return and(conditions);
    }

    @Override
    public BooleanFormula getConstraints() {
        return and(rho);
    }

    public BooleanFormula lessOrEquals(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
        List<BooleanFormula> conditions = new ArrayList<>();
        List<BooleanVariable> aVars = a.getVars();
        List<BooleanVariable> bVars = b.getVars();
        for (int i = 0; i < aVars.size(); i++) {
            conditions.add(imply(aVars.get(i), bVars.get(i)));
        }
        return and(conditions);
    }

    @Override
    public BooleanFormula less(ThermometerBooleanFormula a, ThermometerBooleanFormula b) {
        return not(lessOrEquals(b, a));
    }

}
